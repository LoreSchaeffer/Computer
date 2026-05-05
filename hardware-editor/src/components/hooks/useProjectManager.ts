import {useCallback, useEffect} from 'react';
import {useCanvasContext} from "../../context/CanvasContext.tsx";
import {useWorkspaceContext} from "../../context/WorkspaceContext.tsx";
import {DEF_CHIP_COLOR, DEF_CHIP_GROUP, DEF_CHIP_NAME, STORAGE_KEY_STATE} from "../../utils/consts.ts";
import {useToast} from "../../context/ToastContext.tsx";
import { useReactFlow } from "@xyflow/react";

export function useProjectManager() {
    const {nodes, edges, clearNodes, restoreCanvas} = useCanvasContext();
    const {chipName, chipColor, chipGroup, setChipName, setChipColor, setChipGroup, workspaceHandle, connectWorkspace} = useWorkspaceContext();
    const {showToast} = useToast();
    const {fitView} = useReactFlow();

    useEffect(() => {
        try {
            localStorage.setItem(STORAGE_KEY_STATE, JSON.stringify({nodes, edges, chipName, chipColor, chipGroup}));
        } catch (error) {
            console.error("Failed to auto-save:", error);
        }
    }, [nodes, edges, chipName, chipColor, chipGroup]);

    const newChip = useCallback(() => {
        clearNodes();
        setChipName(DEF_CHIP_NAME);
        setChipColor(DEF_CHIP_COLOR);
        setChipGroup(DEF_CHIP_GROUP);
        localStorage.removeItem(STORAGE_KEY_STATE);
    }, [clearNodes, setChipName, setChipColor, setChipGroup]);

    const openChip = useCallback(async (chip?: string): Promise<{ success: boolean, error?: string }> => {
        try {
            let fileHandle;

            if (chip) {
                if (!workspaceHandle) return {success: false, error: "Workspace not connected."};

                const fileName = `${chip}.json`;
                try {
                    fileHandle = await workspaceHandle.getFileHandle(fileName);
                } catch {
                    return {success: false, error: `File ${fileName} not found.`};
                }
            } else {
                const handles = await (window as any).showOpenFilePicker({
                    types: [{description: 'JSON Files', accept: {'application/json': ['.json']}}]
                });
                fileHandle = handles[0];
            }

            const file = await fileHandle.getFile();
            const text = await file.text();
            const json = JSON.parse(text);

            setChipName(json.chipName || DEF_CHIP_NAME);
            setChipGroup(json.chipGroup || json.group || DEF_CHIP_GROUP);
            setChipColor(json.chipColor || DEF_CHIP_COLOR);

            if (json.layout?.nodes && json.layout?.edges) {
                restoreCanvas(json.layout.nodes, json.layout.edges);

                window.requestAnimationFrame(() => {
                    fitView({
                        duration: 800,
                        padding: 0.2
                    });
                });

                return {success: true};
            }

            return {success: false, error: "File loaded, but lacks visual layout coordinates."};
        } catch {
            return {success: false, error: "Operation cancelled or failed to read the file."};
        }
    }, [setChipName, setChipGroup, setChipColor, restoreCanvas, workspaceHandle]);

    const saveChip = useCallback(async (): Promise<{ success: boolean, error?: string }> => {
        // 1. Validation Phase
        for (const node of nodes) {
            // Added 'flipflop' to the whitelist of processable components
            if (node.type === 'logicGate' || node.type === 'latch' || node.type === 'flipflop' || node.type === 'customChip') {
                const isSource: boolean = node.data.typeLabel === 'VCC' || node.data.typeLabel === 'GND';
                const hasInput: boolean = edges.some(e => e.target === node.id);
                const hasOutput: boolean = edges.some(e => e.source === node.id);

                if (isSource) {
                    if (!hasOutput) {
                        return {success: false, error: `The constant source "${node.data.label}" must be connected.`};
                    }
                } else {
                    if (!hasInput || !hasOutput) {
                        return {success: false, error: `Component "${node.data.label}" needs connections.`};
                    }
                }
            }
        }

        // Helper function to resolve dynamic pin names
        const getPinName = (label: string, handle: string | null | undefined, defaultHandle: string): string => {
            if (!handle || handle === defaultHandle) return label;
            const match: RegExpMatchArray | null = handle.match(/\d+$/);
            return match ? `${label}${match[0]}` : label;
        };

        // 2. I/O Extraction
        const chipInputs: string[] = [];
        nodes.filter(n => n.type === 'inputPin').forEach(node => {
            const outputs: string[] = (node.data.outputs as string[]) || ['Out'];
            outputs.forEach(handle => chipInputs.push(getPinName(node.data.label as string, handle, 'Out')));
        });

        const chipOutputs: string[] = [];
        nodes.filter(n => n.type === 'outputPin').forEach(node => {
            const inputs: string[] = (node.data.inputs as string[]) || ['In'];
            inputs.forEach(handle => chipOutputs.push(getPinName(node.data.label as string, handle, 'In')));
        });

        // 3. Routing and Netlist Generation
        const nets: Map<string, string> = new Map<string, string>();
        const internalWires: string[] = [];

        edges.forEach(edge => {
            const sourceKey: string = `${edge.source}_${edge.sourceHandle || 'Out'}`;

            if (!nets.has(sourceKey)) {
                const sourceNode = nodes.find(n => n.id === edge.source);
                const isInputPin: boolean = sourceNode?.type === 'inputPin';

                const targetEdge = edges.find(e =>
                    e.source === edge.source &&
                    e.sourceHandle === edge.sourceHandle &&
                    nodes.find(nt => nt.id === e.target)?.type === 'outputPin'
                );

                const targetIsOutput: boolean = !!targetEdge;

                if (isInputPin && sourceNode) {
                    nets.set(sourceKey, getPinName(sourceNode.data.label as string, edge.sourceHandle, 'Out'));
                } else if (targetIsOutput && targetEdge) {
                    const outNode = nodes.find(n => n.id === targetEdge.target);
                    if (outNode) {
                        nets.set(sourceKey, getPinName(outNode.data.label as string, targetEdge.targetHandle, 'In'));
                    }
                } else if (sourceNode) {
                    const cleanName: string = String(sourceNode.data.label).toLowerCase().replace(/\W/g, '');
                    const wireName: string = `wire_${cleanName}_${sourceNode.id.slice(-4)}${edge.sourceHandle ? '_' + edge.sourceHandle : ''}`;

                    nets.set(sourceKey, wireName);
                    if (!internalWires.includes(wireName)) {
                        internalWires.push(wireName);
                    }
                }
            }
        });

        // 4. Component Mapping
        // Added 'flipflop' to the mapped components list
        const components = nodes.filter(n => ['logicGate', 'latch', 'flipflop', 'customChip'].includes(n.type!)).map(node => {
            const inputsRecord: Record<string, string> = {};
            const outputsRecord: Record<string, string[]> = {};

            ((node.data.inputs as string[]) || []).forEach(pinName => {
                const edge = edges.find(e => e.target === node.id && e.targetHandle === pinName);
                inputsRecord[pinName] = edge ? (nets.get(`${edge.source}_${edge.sourceHandle || 'Out'}`) || 'NC') : 'NC';
            });

            ((node.data.outputs as string[]) || []).forEach(pinName => {
                const outgoingEdges = edges.filter(e => e.source === node.id && e.sourceHandle === pinName);

                if (outgoingEdges.length > 0) {
                    const targetNames: string[] = outgoingEdges.map(edge => {
                        const targetNode = nodes.find(n => n.id === edge.target);
                        if (targetNode?.type === 'outputPin') {
                            return getPinName(targetNode.data.label as string, edge.targetHandle, 'In');
                        }
                        return nets.get(`${edge.source}_${edge.sourceHandle || 'Out'}`) || 'UNKNOWN_WIRE';
                    });

                    outputsRecord[pinName] = Array.from(new Set(targetNames));
                } else {
                    outputsRecord[pinName] = [];
                }
            });

            return {
                type: node.data.typeLabel as string,
                name: node.data.label as string,
                inputs: inputsRecord,
                outputs: outputsRecord
            };
        });

        // 5. Cleanup Data
        const simplifiedNodes = nodes.map(node => {
            const {
                values,
                internalComponents,
                internalState,
                ...cleanData
            } = node.data as any;

            return { ...node, data: cleanData };
        });

        // 6. Assembly JSON Payload
        const exportData = {
            chipName,
            chipColor,
            chipGroup,
            pins: {
                inputs: chipInputs,
                outputs: chipOutputs
            },
            internalWires,
            components,
            layout: {
                nodes: simplifiedNodes,
                edges
            }
        };

        const fileName: string = `${chipName}.json`;
        const jsonString: string = JSON.stringify(exportData, null, 2);

        // 7. Write to Disk
        if (workspaceHandle) {
            try {
                const fileHandle = await workspaceHandle.getFileHandle(fileName, {create: true});
                const writable = await fileHandle.createWritable();
                await writable.write(jsonString);
                await writable.close();

                showToast('success', 'Chip Saved!', `Saved as ${fileName}`);
                await connectWorkspace();

                return {success: true};
            } catch (err) {
                console.error("Save error:", err);
            }
        }

        // Fallback if Workspace is not connected
        const blob: Blob = new Blob([jsonString], {type: 'application/json'});
        const url: string = URL.createObjectURL(blob);
        const a: HTMLAnchorElement = document.createElement('a');

        a.href = url;
        a.download = fileName;
        a.click();
        URL.revokeObjectURL(url);

        return {success: true};

    }, [nodes, edges, chipName, chipColor, chipGroup, workspaceHandle, connectWorkspace, showToast]);

    return {newChip, openChip, saveChip};
}