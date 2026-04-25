import {createContext, type ReactNode, useCallback, useContext, useEffect, useState} from 'react';
import {addEdge, applyEdgeChanges, applyNodeChanges, type Connection, type Edge, type EdgeChange, type Node, type NodeChange} from '@xyflow/react';
import {v4 as uuidv4} from 'uuid';
import type {ChipDefinition, HardwareTemplate} from "../types/HardwareTypes.ts";
import {loadDirectoryHandle, saveDirectoryHandle} from "../utils/IndexedDB.ts";
import {toSnakeCase} from "../utils/utils.ts";
import {DEF_CHIP_COLOR, DEF_CHIP_GROUP, DEF_CHIP_NAME} from "../utils/consts.ts";

const STORAGE_KEY = 'hw_editor_state';

const IO_NODES: HardwareTemplate[] = [
    {type: 'inputPin', data: {typeLabel: 'INPUT', label: 'IN', group: 'I/O Nodes'}},
    {type: 'outputPin', data: {typeLabel: 'OUTPUT', label: 'OUT', group: 'I/O Nodes'}}
];

const GATE_NODES: HardwareTemplate[] = [
    {type: 'logicGate', data: {typeLabel: 'AndGate', label: 'AND', inputs: ['A', 'B'], outputs: ['OUT'], group: 'Logic Gates'}},
    {type: 'logicGate', data: {typeLabel: 'NandGate', label: 'NAND', inputs: ['A', 'B'], outputs: ['OUT'], group: 'Logic Gates'}},
    {type: 'logicGate', data: {typeLabel: 'OrGate', label: 'OR', inputs: ['A', 'B'], outputs: ['OUT'], group: 'Logic Gates'}},
    {type: 'logicGate', data: {typeLabel: 'NorGate', label: 'NOR', inputs: ['A', 'B'], outputs: ['OUT'], group: 'Logic Gates'}},
    {type: 'logicGate', data: {typeLabel: 'XorGate', label: 'XOR', inputs: ['A', 'B'], outputs: ['OUT'], group: 'Logic Gates'}},
    {type: 'logicGate', data: {typeLabel: 'NotGate', label: 'NOT', inputs: ['IN'], outputs: ['OUT'], group: 'Logic Gates'}},
];

interface SavedState {
    nodes: Node[];
    edges: Edge[];
    chipName: string;
    chipColor: string;
    chipGroup: string;
}

interface EditorContextType {
    // --- Canvas State ---
    nodes: Node[];
    edges: Edge[];
    clipboard: Node[];
    onNodesChange: (changes: NodeChange[]) => void;
    onEdgesChange: (changes: EdgeChange[]) => void;
    onConnect: (connection: Connection) => void;

    // --- Project State ---
    chipName: string;
    chipColor: string;
    chipGroup: string;
    setChipName: (name: string) => void;
    setChipColor: (color: string) => void;
    setChipGroup: (group: string) => void;

    // --- Workspace State ---
    library: HardwareTemplate[];
    groups: string[];
    isWorkspaceConnected: boolean;
    workspaceHandle: any | null;

    // --- Actions: Canvas ---
    addNode: (node: Node) => void;
    updateCustomNodeData: (id: string, newData: Record<string, unknown>) => void;
    setClipboard: (nodes: Node[]) => void;
    cloneNodes: (nodesToClone: Node[], targetPosition?: { x: number, y: number }) => void;
    clearNodes: () => void;

    // --- Actions: Project & File System ---
    newChip: () => void;
    openChip: () => Promise<{ success: boolean, error?: string }>;
    saveChip: () => Promise<{ success: boolean, error?: string }>;
    connectWorkspace: (forcePicker?: boolean) => Promise<void>;
}

const EditorContext = createContext<EditorContextType | undefined>(undefined);

export function EditorProvider({children}: { children: ReactNode }) {
    // 1. STATE INITIALIZATION
    const loadInitialState = (): SavedState => {
        try {
            const saved = localStorage.getItem(STORAGE_KEY);
            if (saved) return JSON.parse(saved) as SavedState;
        } catch (error) {
            console.error("Failed to parse state from LocalStorage:", error);
        }
        return {nodes: [], edges: [], chipName: 'UntitledChip', chipColor: '#1e3799', chipGroup: 'Custom Chips'};
    }

    const initialState = loadInitialState();

    const [nodes, setNodes] = useState<Node[]>(initialState.nodes);
    const [edges, setEdges] = useState<Edge[]>(initialState.edges);
    const [clipboard, setClipboard] = useState<Node[]>([]);

    const [chipName, setChipName] = useState<string>(initialState.chipName);
    const [chipColor, setChipColor] = useState<string>(initialState.chipColor);
    const [chipGroup, setChipGroup] = useState<string>(initialState.chipGroup);

    const [library, setLibrary] = useState<HardwareTemplate[]>([]);
    const [groups, setGroups] = useState<string[]>(['Custom Chips']);
    const [isWorkspaceConnected, setIsWorkspaceConnected] = useState(false);
    const [workspaceHandle, setWorkspaceHandle] = useState<any | null>(null);

    // 2. EFFECTS
    useEffect(() => {
        try {
            const stateToSave: SavedState = {nodes, edges, chipName, chipColor, chipGroup};
            localStorage.setItem(STORAGE_KEY, JSON.stringify(stateToSave));
        } catch (error) {
            console.error("Failed to save state to LocalStorage:", error);
        }
    }, [nodes, edges, chipName, chipColor, chipGroup]);

    useEffect(() => {
        connectWorkspace().catch(() => setLibrary([...IO_NODES, ...GATE_NODES]));
    }, []);

    // 3. ACTIONS: CANVAS
    const onNodesChange = useCallback((changes: NodeChange[]) => setNodes((nds) => applyNodeChanges(changes, nds)), []);
    const onEdgesChange = useCallback((changes: EdgeChange[]) => setEdges((eds) => applyEdgeChanges(changes, eds)), []);
    const onConnect = useCallback((connection: Connection) => setEdges((eds) => addEdge(connection, eds)), []);
    const addNode = useCallback((node: Node) => {
        setNodes((currentNodes) => {
            const unselectedNodes = currentNodes.map(n => ({...n, selected: false}));
            return [...unselectedNodes, node];
        });
    }, []);

    const updateCustomNodeData = useCallback((id: string, newData: Record<string, unknown>) => {
        setNodes((nds) => nds.map((node) => node.id === id ? {...node, data: {...node.data, ...newData}} : node));
    }, []);

    const cloneNodes = useCallback((nodesToClone: Node[], targetPosition?: { x: number, y: number }) => {
        setNodes((currentNodes) => {
            const minX = Math.min(...nodesToClone.map(n => n.position.x));
            const minY = Math.min(...nodesToClone.map(n => n.position.y));

            const clonedNodes = nodesToClone.map(node => {
                let newPos;

                if (targetPosition) {
                    const offsetX = node.position.x - minX;
                    const offsetY = node.position.y - minY;
                    newPos = {
                        x: targetPosition.x + offsetX,
                        y: targetPosition.y + offsetY
                    };
                } else {
                    newPos = {
                        x: node.position.x + 50,
                        y: node.position.y + 50
                    };
                }

                return {
                    ...node,
                    id: `node_${uuidv4()}`,
                    position: newPos,
                    origin: [0.5, 0.5] as [number, number],
                    selected: true,
                    data: {...node.data}
                };
            });

            const unselectedCurrent = currentNodes.map(n => ({...n, selected: false}));
            return [...unselectedCurrent, ...clonedNodes];
        });
    }, []);

    const clearNodes = useCallback(() => {
        setNodes([]);
        setEdges([]);
    }, []);

    // 4. ACTIONS: PROJECT & FS
    const newChip = useCallback(() => {
        setNodes([]);
        setEdges([]);
        setChipName(DEF_CHIP_NAME);
        setChipColor(DEF_CHIP_COLOR);
        setChipGroup(DEF_CHIP_GROUP);
        localStorage.removeItem(STORAGE_KEY);
    }, []);

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

            if (json.layout && json.layout.nodes && json.layout.edges) {
                setNodes(json.layout.nodes);
                setEdges(json.layout.edges);
                return {success: true};
            } else {
                return {success: false, error: "File loaded, but it does not contain visual layout information (coordinates)."};
            }
        } catch (e) {
            console.error("Failed to open file", e);
            return {success: false, error: "Operation cancelled or failed to read the file."};
        }
    }, []);

    const saveChip = useCallback(async (): Promise<{ success: boolean, error?: string }> => {
        // Validation
        for (const node of nodes) {
            if (node.type === 'logicGate') {
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
                    const wireName = `wire_${cleanSourceName}${handleName}`;
                    nets.set(sourceKey, wireName);
                    if (!internalWires.includes(wireName)) internalWires.push(wireName);
                }
            }
        });

        const components: any[] = [];
        nodes.filter(n => n.type === 'logicGate').forEach(node => {
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

        return {success: true};
    }, [nodes, edges, chipName, chipColor, chipGroup, workspaceHandle]);

    // 5. ACTIONS: WORKSPACE
    const connectWorkspace = async (forcePicker = false) => {
        try {
            let handle = forcePicker ? null : await loadDirectoryHandle();
            if (!handle) {
                handle = await (window as any).showDirectoryPicker({mode: 'readwrite'});
                await saveDirectoryHandle(handle!);
            } else {
                const permission = await (handle as any).requestPermission({mode: 'readwrite'});
                if (permission !== 'granted') throw new Error("Permission denied");
            }
            setWorkspaceHandle(handle);

            const loadedChips: HardwareTemplate[] = [];
            const foundGroups = new Set<string>(['Primitives']);

            for await (const entry of (handle as any).values()) {
                if (entry.kind === 'file' && entry.name.endsWith('.json')) {
                    const file = await entry.getFile();
                    const text = await file.text();
                    try {
                        const json = JSON.parse(text) as ChipDefinition;
                        const groupName = json.group || 'Custom Chips';
                        foundGroups.add(groupName);

                        loadedChips.push({
                            type: 'logicGate',
                            data: {
                                typeLabel: json.chipName, label: json.chipName,
                                inputs: json.pins?.inputs || [], outputs: json.pins?.outputs || [],
                                headerColor: json.chipColor || '#1e3799', group: groupName
                            }
                        });
                    } catch (err) {
                        console.warn("Failed to parse JSON:", entry.name);
                    }
                }
            }
            setGroups(Array.from(foundGroups).sort());
            setLibrary([...IO_NODES, ...GATE_NODES, ...loadedChips.sort((a, b) => a.data.label.localeCompare(b.data.label))]);
            setIsWorkspaceConnected(true);
        } catch (error) {
            console.error("Workspace connection failed:", error);
            setLibrary([...IO_NODES, ...GATE_NODES]);
        }
    };

    return (
        <EditorContext.Provider value={{
            nodes,
            edges,
            clipboard,
            onNodesChange,
            onEdgesChange,
            onConnect,
            chipName,
            chipColor,
            chipGroup,
            setChipName,
            setChipColor,
            setChipGroup,
            library,
            groups,
            isWorkspaceConnected,
            workspaceHandle,
            addNode,
            updateCustomNodeData,
            setClipboard,
            cloneNodes,
            clearNodes,
            newChip,
            openChip,
            saveChip,
            connectWorkspace,
        }}>
            {children}
        </EditorContext.Provider>
    );
}

export const useEditorContext = (): EditorContextType => {
    const context = useContext(EditorContext);
    if (!context) throw new Error("useEditorContext must be used within an EditorProvider");
    return context;
};