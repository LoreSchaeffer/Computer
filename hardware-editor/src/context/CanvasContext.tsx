import {createContext, type PropsWithChildren, useCallback, useContext, useEffect, useState} from 'react';
import {addEdge, applyEdgeChanges, applyNodeChanges, type Connection, type Edge, type EdgeChange, type Node, type NodeChange} from '@xyflow/react';
import {v4 as uuidv4} from 'uuid';
import {STORAGE_KEY_STATE} from "../utils/consts.ts";
import {useWorkspaceContext} from "./WorkspaceContext.tsx";
import {GATE_LOGIC, getExpectedOutputs, SEQUENTIAL_LOGIC} from "../utils/logicEngine.ts";

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
    toggleInput: (nodeId: string, outputPort: string) => void;
    runSimulation: () => void;
    simulationEnabled: boolean;
    toggleSimulationEnabled: (val: boolean) => void;
}

const CanvasContext = createContext<CanvasContextType | undefined>(undefined);

export function CanvasProvider({children}: PropsWithChildren) {
    const {library, isWorkspaceReady} = useWorkspaceContext();
    const [simulationEnabled, setSimulationEnabled] = useState<boolean>(true);

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
        if (!isWorkspaceReady) return;
        if (!simulationEnabled) return;

        setNodes((nds) => {
            let currentNodes = nds;
            const edgeMap = edges;

            const evaluateComponent = (
                typeLabel: string,
                inputs: boolean[],
                compName: string,
                nodeInternalState: Record<string, any>,
                path: string[] = [],
                typePath: string[] = []
            ): Record<string, boolean> => {
                const targetType = typeLabel.trim();
                const statePath = [...path, compName].join('/');

                if (typePath.includes(targetType)) {
                    console.error(`Infinite loop detected: The chip "${typeLabel}" is trying to contain itself!`);
                    return {};
                }

                if (GATE_LOGIC[targetType]) return {'Out': GATE_LOGIC[targetType](inputs)};

                if (SEQUENTIAL_LOGIC[targetType]) {
                    const prevState = nodeInternalState[statePath] || {};
                    const result = SEQUENTIAL_LOGIC[targetType](inputs, prevState);
                    nodeInternalState[statePath] = {values: result};
                    return result;
                }

                const chipDef = library.find(c =>
                    c.data.typeLabel?.trim().toLowerCase() === targetType.toLowerCase() ||
                    c.data.label?.trim().toLowerCase() === targetType.toLowerCase()
                );

                if (!chipDef) {
                    const availableChips = library.map(c => c.data.typeLabel).join(', ');
                    console.warn(`Simulation interrupted: Component "${targetType}" not found in your Library.\nAvailable chips in memory: [${availableChips}]\nMake sure that its JSON is saved in your workspace!`);

                    return {};
                }

                if (chipDef.data.internalComponents) {
                    const nets: Record<string, boolean> = {};
                    (chipDef.data.inputs || []).forEach((pin, i) => nets[pin] = inputs[i] || false);

                    for (let step = 0; step < 5; step++) {
                        chipDef.data.internalComponents.forEach((comp: any) => {

                            const expectedInPins = library.find(c => c.data.typeLabel === comp.type)?.data.inputs || [];
                            const expectedOutPins = getExpectedOutputs(comp.type, library);

                            const compIns = expectedInPins.map(pinName => {
                                const wireName = comp.inputs[pinName];
                                return nets[wireName] || false;
                            });

                            const subOuts = evaluateComponent(
                                comp.type,
                                compIns,
                                comp.name,
                                nodeInternalState,
                                [...path, compName],
                                [...typePath, targetType]
                            );

                            expectedOutPins.forEach(pinName => {
                                const targetWires = comp.outputs[pinName];

                                if (Array.isArray(targetWires)) {
                                    targetWires.forEach(wireName => nets[wireName] = subOuts[pinName] || false);
                                } else if (typeof targetWires === 'string') {
                                    nets[targetWires] = subOuts[pinName] || false;
                                }
                            });
                        });
                    }
                    const result: Record<string, boolean> = {};
                    (chipDef.data.outputs || []).forEach(pin => {
                        result[pin] = nets[pin] || false;
                    });
                    return result;
                }

                return {};
            };

            for (let i = 0; i < 5; i++) {
                currentNodes = currentNodes.map((node) => {
                    if (node.type === 'inputPin') return node;

                    const nodeData = node.data as any;
                    const newValues = {...(nodeData.values || {})};

                    const newInternalState: Record<string, any> = {...(nodeData.internalState || {})};

                    const inputPins = nodeData.inputs || [];

                    const inputValues: boolean[] = inputPins.map((pinId: string) => {
                        const connection = edgeMap.find(e => e.target === node.id && e.targetHandle === pinId);
                        if (!connection) return false;

                        const sourceNode = currentNodes.find(n => n.id === connection.source);
                        const sourceNodeData = sourceNode?.data as any;
                        const handleName = connection.sourceHandle || (sourceNodeData.outputs && sourceNodeData.outputs[0]) || 'Out';

                        return sourceNodeData?.values?.[handleName] || false;
                    });

                    inputPins.forEach((pinId: string, index: number) => {
                        newValues[pinId] = inputValues[index];
                    });

                    if (node.type === 'outputPin') {
                        newValues['In'] = inputValues[0] || false;
                    } else {
                        const outResults = evaluateComponent(nodeData.typeLabel, inputValues, 'root', newInternalState, [], []);
                        Object.assign(newValues, outResults);
                    }

                    if (JSON.stringify(nodeData.values) !== JSON.stringify(newValues) ||
                        JSON.stringify(nodeData.internalState) !== JSON.stringify(newInternalState)) {
                        return {...node, data: {...nodeData, values: newValues, internalState: newInternalState}};
                    }

                    return node;
                });
            }
            return currentNodes;
        });
    }, [edges, library, isWorkspaceReady]);

    const toggleInput = useCallback((nodeId: string, outputPort: string = 'Out') => {
        setNodes(nds => nds.map(node => {
            if (node.id === nodeId) {
                const nodeData = node.data as any;
                const currentVal = nodeData.values?.[outputPort] || false;
                return {
                    ...node,
                    data: {
                        ...nodeData,
                        values: {...(nodeData.values || {}), [outputPort]: !currentVal}
                    }
                };
            }
            return node;
        }));

        setTimeout(() => runSimulation(), 0);
    }, [runSimulation]);

    const toggleSimulationEnabled = useCallback((val: boolean) => {
        setSimulationEnabled(val);
    }, [setSimulationEnabled]);

    useEffect(() => {
        runSimulation();
    }, [edges, runSimulation]);

    useEffect(() => {
        if (!isWorkspaceReady || library.length === 0 || nodes.length === 0) return;

        let changed = false;
        const updatedNodes = nodes.map(node => {
            if (node.type === 'inputPin' || node.type === 'outputPin') return node;

            const template = library.find(t => t.data.typeLabel === node.data.typeLabel);
            if (!template) return node;

            const tInputs = template.data.inputs || [];
            const tOutputs = template.data.outputs || [];
            const currentInputs = (node.data.inputs as string[]) || [];
            const currentOutputs = (node.data.outputs as string[]) || [];

            if (JSON.stringify(tInputs) !== JSON.stringify(currentInputs) ||
                JSON.stringify(tOutputs) !== JSON.stringify(currentOutputs)) {
                changed = true;
                return {...node, data: {...node.data, inputs: tInputs, outputs: tOutputs}};
            }
            return node;
        });

        if (changed) setNodes(updatedNodes);
    }, [library, isWorkspaceReady, nodes.length]);

    useEffect(() => {
        if (nodes.length === 0 || edges.length === 0) return;

        let changed = false;
        const updatedEdges = edges.map(edge => {
            const sourceNode = nodes.find(n => n.id === edge.source);
            const targetNode = nodes.find(n => n.id === edge.target);

            let isOrphan = false;
            if (sourceNode && sourceNode.type !== 'inputPin') {
                const outputs = (sourceNode.data.outputs as string[]) || [];
                if (!outputs.includes(edge.sourceHandle || 'Out')) isOrphan = true;
            }
            if (targetNode && targetNode.type !== 'outputPin') {
                const inputs = (targetNode.data.inputs as string[]) || [];
                if (!inputs.includes(edge.targetHandle || 'In')) isOrphan = true;
            }

            const currentStyleStr = JSON.stringify(edge.style);
            const targetStyle = isOrphan
                ? {stroke: 'var(--color-error)', strokeWidth: 3, strokeDasharray: '5,5'}
                : {strokeWidth: 2, transition: 'stroke 0.2s'};

            if (currentStyleStr !== JSON.stringify(targetStyle)) {
                changed = true;
                return {...edge, style: targetStyle, animated: isOrphan};
            }
            return edge;
        });

        if (changed) setEdges(updatedEdges);
    }, [nodes, edges.length]);

    return (
        <CanvasContext.Provider value={{
            nodes,
            edges,
            clipboard,
            onNodesChange,
            onEdgesChange,
            onConnect,
            addNode,
            updateCustomNodeData,
            setClipboard,
            cloneNodes,
            clearNodes,
            restoreCanvas,
            toggleInput,
            runSimulation,
            simulationEnabled,
            toggleSimulationEnabled
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