import {useCallback, useEffect} from 'react';
import {useCanvasContext} from "../../context/CanvasContext.tsx";
import {useWorkspaceContext} from "../../context/WorkspaceContext.tsx";
import {DEF_CHIP_COLOR, DEF_CHIP_GROUP, DEF_CHIP_NAME, STORAGE_KEY_STATE} from "../../utils/consts.ts";
import {toSnakeCase} from "../../utils/utils.ts";
import {useToast} from "../../context/ToastContext.tsx";

export function useProjectManager() {
    const {nodes, edges, clearNodes, restoreCanvas} = useCanvasContext();
    const {chipName, chipColor, chipGroup, setChipName, setChipColor, setChipGroup, workspaceHandle, connectWorkspace} = useWorkspaceContext();
    const {showToast} = useToast();

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

    const openChip = useCallback(async (): Promise<{ success: boolean, error?: string }> => {
        try {
            const [fileHandle] = await (window as any).showOpenFilePicker({
                types: [{description: 'JSON Files', accept: {'application/json': ['.json']}}]
            });
            const file = await fileHandle.getFile();
            const text = await file.text();
            const json = JSON.parse(text);

            setChipName(json.chipName || DEF_CHIP_NAME);
            setChipGroup(json.chipGroup || json.group || DEF_CHIP_GROUP);
            setChipColor(json.chipColor || DEF_CHIP_COLOR);

            if (json.layout?.nodes && json.layout?.edges) {
                restoreCanvas(json.layout.nodes, json.layout.edges);
                return {success: true};
            }
            return {success: false, error: "File loaded, but lacks visual layout coordinates."};
        } catch (e) {
            return {success: false, error: "Operation cancelled or failed to read the file."};
        }
    }, [setChipName, setChipGroup, setChipColor, restoreCanvas]);

    const saveChip = useCallback(async (): Promise<{ success: boolean, error?: string }> => {
        // Validation
        for (const node of nodes) {
            if (node.type === 'logicGate' || node.type === 'latch' || node.type === 'customChip') {
                const hasInput = edges.some(e => e.target === node.id);
                const hasOutput = edges.some(e => e.source === node.id);
                if (!hasInput || !hasOutput) return {
                    success: false,
                    error: `The component "${node.data.label}" must have at least one input and one output connected.`
                };
            } else if (node.type === 'inputPin') {
                if (!edges.some(e => e.source === node.id)) return {
                    success: false,
                    error: `The input pin "${node.data.label}" is not connected.`
                };
            } else if (node.type === 'outputPin') {
                if (!edges.some(e => e.target === node.id)) return {
                    success: false,
                    error: `The output pin "${node.data.label}" is not connected.`
                };
            }
        }

        const inputNodes = nodes.filter(n => n.type === 'inputPin');
        const outputNodes = nodes.filter(n => n.type === 'outputPin');
        const chipInputs = inputNodes.map(n => n.data.label as string);
        const chipOutputs = outputNodes.map(n => n.data.label as string);

        const nets = new Map<string, string>();
        const internalWires: string[] = [];

        edges.forEach(edge => {
            const sourceKey = `${edge.source}_${edge.sourceHandle || 'out'}`;
            if (!nets.has(sourceKey)) {
                const sourceNode = nodes.find(n => n.id === edge.source);
                const siblingEdges = edges.filter(e => e.source === edge.source && e.sourceHandle === edge.sourceHandle);
                const outputPinTarget = siblingEdges.map(e => nodes.find(n => n.id === e.target)).find(n => n?.type === 'outputPin');

                if (sourceNode?.type === 'inputPin') {
                    nets.set(sourceKey, sourceNode.data.label as string);
                } else if (outputPinTarget) {
                    nets.set(sourceKey, outputPinTarget.data.label as string);
                } else {
                    const cleanSourceName = String(sourceNode?.data.label).toLowerCase().replace(/[^a-z0-9_]/g, '');
                    const handleName = edge.sourceHandle ? `_${edge.sourceHandle.toLowerCase()}` : '';
                    const uniqueHash = sourceNode?.id.replace('node_', '').substring(0, 6) || 'unk';
                    const wireName = `wire_${cleanSourceName}_${uniqueHash}${handleName}`;
                    nets.set(sourceKey, wireName);
                    if (!internalWires.includes(wireName)) internalWires.push(wireName);
                }
            }
        });

        const components: any[] = [];

        nodes.filter(n => n.type === 'logicGate' || n.type === 'latch' || n.type === 'customChip').forEach(node => {
            const compInputs = ((node.data.inputs as string[]) || []).map(pinName => {
                const incomingEdge = edges.find(e => e.target === node.id && e.targetHandle === pinName);
                return incomingEdge ? (nets.get(`${incomingEdge.source}_${incomingEdge.sourceHandle || 'out'}`) || 'UNCONNECTED') : 'UNCONNECTED';
            });
            const compOutputs = ((node.data.outputs as string[]) || []).map(pinName => nets.get(`${node.id}_${pinName}`)!).filter(Boolean);

            components.push({type: node.data.typeLabel, name: node.data.label, inputs: compInputs, outputs: compOutputs});
        });

        const exportData = {
            chipName, chipColor, chipGroup, group: chipGroup,
            pins: {inputs: chipInputs, outputs: chipOutputs},
            internalWires, components,
            layout: {nodes, edges}
        };

        const jsonString = JSON.stringify(exportData, null, 2);
        const fileName = `${toSnakeCase(chipName)}.json`;

        if (workspaceHandle) {
            try {
                const fileHandle = await workspaceHandle.getFileHandle(fileName, {create: true});
                const writable = await fileHandle.createWritable();
                await writable.write(jsonString);
                await writable.close();

                showToast('success', 'Chip Saved!', `Saved successfully to your workspace.`);
                await connectWorkspace();
                return {success: true};
            } catch (err) {
                console.error("Failed to write to workspace.", err);
            }
        }

        // Fallback Download
        const blob = new Blob([jsonString], {type: 'application/json'});
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);

        showToast('success', 'Chip Exported', `Downloaded ${fileName} to your computer.`);
        await connectWorkspace();
        return {success: true};
    }, [nodes, edges, chipName, chipColor, chipGroup, workspaceHandle]);

    return {newChip, openChip, saveChip};
}