import {createContext, type PropsWithChildren, useCallback, useContext, useEffect, useState} from 'react';
import {addEdge, applyEdgeChanges, applyNodeChanges, type Connection, type Edge, type EdgeChange, type Node, type NodeChange} from '@xyflow/react';
import {v4 as uuidv4} from 'uuid';
import {STORAGE_KEY_STATE} from "../utils/consts.ts";

const GATE_LOGIC: Record<string, (inputs: boolean[]) => boolean> = {
    'AndGate': (ins) => ins.length > 0 && ins.every(v => v),
    'OrGate': (ins) => ins.some(v => v),
    'NotGate': (ins) => !ins[0],
    'NandGate': (ins) => !(ins.length > 0 && ins.every(v => v)),
    'NorGate': (ins) => !ins.some(v => v),
    'XorGate': (ins) => ins.filter(v => v).length % 2 !== 0,
};

interface CanvasContextType {
    nodes: Node[];
    edges: Edge[];
    clipboard: Node[];
    onNodesChange: (changes: NodeChange[]) => void;
    onEdgesChange: (changes: EdgeChange[]) => void;
    onConnect: (connection: Connection) => void;
    addNode: (node: Node) => void;
    updateCustomNodeData: (id: string, newData: Record<string, unknown>) => void;
    setClipboard: (nodes: Node[]) => void;
    cloneNodes: (nodesToClone: Node[], targetPosition?: { x: number, y: number }) => void;
    clearNodes: () => void;
    restoreCanvas: (nodes: Node[], edges: Edge[]) => void;
    toggleInput: (nodeId: string) => void;
}

const CanvasContext = createContext<CanvasContextType | undefined>(undefined);

export function CanvasProvider({children}: PropsWithChildren) {
    const getInitialCanvasState = () => {
        try {
            const saved = localStorage.getItem(STORAGE_KEY_STATE);
            if (saved) {
                const parsed = JSON.parse(saved);
                return {nodes: parsed.nodes || [], edges: parsed.edges || []};
            }
        } catch (e) {
        }
        return {nodes: [], edges: []};
    };

    const initialState = getInitialCanvasState();
    const [nodes, setNodes] = useState<Node[]>(initialState.nodes);
    const [edges, setEdges] = useState<Edge[]>(initialState.edges);
    const [clipboard, setClipboard] = useState<Node[]>([]);

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
                const newPos = targetPosition
                    ? {x: targetPosition.x + (node.position.x - minX), y: targetPosition.y + (node.position.y - minY)}
                    : {x: node.position.x + 50, y: node.position.y + 50};

                return {
                    ...node, id: `node_${uuidv4()}`, position: newPos,
                    origin: [0.5, 0.5] as [number, number], selected: true, data: {...node.data}
                };
            });
            return [...currentNodes.map(n => ({...n, selected: false})), ...clonedNodes];
        });
    }, []);

    const clearNodes = useCallback(() => {
        setNodes([]);
        setEdges([]);
    }, []);

    const restoreCanvas = useCallback((newNodes: Node[], newEdges: Edge[]) => {
        setNodes(newNodes);
        setEdges(newEdges);
    }, []);

    const runSimulation = useCallback(() => {
        setNodes((nds) => {
            let currentNodes = nds;
            const edgeMap = edges;

            for (let i = 0; i < 10; i++) {
                currentNodes = currentNodes.map((node) => {
                    if (node.type === 'inputPin') return node;

                    const nodeData = node.data as any;
                    const newValues = {...(nodeData.values || {})};

                    const inputPins = node.type === 'outputPin' ? ['in'] : (nodeData.inputs || []);

                    const inputValues: boolean[] = inputPins.map((pinId: string) => {
                        const connection = edgeMap.find(e => e.target === node.id && e.targetHandle === pinId);
                        if (!connection) return false;

                        const sourceNode = currentNodes.find(n => n.id === connection.source);
                        const sourceNodeData = sourceNode?.data as any;
                        return sourceNodeData?.values?.[connection.sourceHandle || 'out'] || false;
                    });

                    inputPins.forEach((pinId: string, index: number) => {
                        newValues[pinId] = inputValues[index];
                    });

                    if (node.type === 'logicGate') {
                        const logicFunc = GATE_LOGIC[nodeData.typeLabel];
                        if (logicFunc) {
                            newValues['OUT'] = logicFunc(inputValues);
                        }
                    } else if (node.type === 'outputPin') {
                        newValues['in'] = inputValues[0] || false;
                    }

                    if (JSON.stringify(nodeData.values) !== JSON.stringify(newValues)) {
                        return {...node, data: {...nodeData, values: newValues}};
                    }

                    return node;
                });
            }
            return currentNodes;
        });
    }, [edges]);

    const toggleInput = useCallback((nodeId: string) => {
        setNodes(nds => nds.map(node => {
            if (node.id === nodeId) {
                const nodeData = node.data as any;
                const currentVal = nodeData.values?.['out'] || false;
                return {
                    ...node,
                    data: {
                        ...nodeData,
                        values: {...(nodeData.values || {}), 'out': !currentVal}
                    }
                };
            }
            return node;
        }));

        setTimeout(() => runSimulation(), 0);
    }, [runSimulation]);

    useEffect(() => {
        runSimulation();
    }, [edges, runSimulation]);

    return (
        <CanvasContext.Provider value={{
            nodes, edges, clipboard, onNodesChange, onEdgesChange, onConnect,
            addNode, updateCustomNodeData, setClipboard, cloneNodes, clearNodes, restoreCanvas, toggleInput
        }}>
            {children}
        </CanvasContext.Provider>
    );
}

export const useCanvasContext = () => {
    const ctx = useContext(CanvasContext);
    if (!ctx) throw new Error("useCanvasContext must be used within CanvasProvider");
    return ctx;
};