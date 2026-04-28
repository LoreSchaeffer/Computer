import styles from './App.module.css';
import {
    Background,
    BackgroundVariant,
    ControlButton,
    Controls,
    MiniMap,
    type Node,
    type NodeTypes,
    ReactFlow,
    SelectionMode,
    useReactFlow
} from '@xyflow/react';
import Header from "./components/Header.tsx";
import Sidebar from "./components/Sidebar.tsx";
import {LogicGateNode} from "./components/hw/LogicGateNode.tsx";
import {InputPinNode} from "./components/hw/InputPinNode.tsx";
import {OutputPinNode} from "./components/hw/OutputPinNode.tsx";
import {type DragEvent, type MouseEvent as ReactMouseEvent, useCallback, useEffect, useMemo, useRef} from "react";
import {v4 as uuidv4} from 'uuid';
import {FaClone, FaCopy, FaCut, FaPaste, FaTrash} from "react-icons/fa";
import {useContextMenu} from "./context/ContextMenuContext.tsx";
import {useModal} from "./context/ModalContext.tsx";
import {INPUT_COLOR, LATCH_COLOR, LOGIC_GATE_COLOR, OUTPUT_COLOR} from "./utils/consts.ts";
import type {GenericNodeData} from "./components/hw/GenericNode.tsx";
import {useCanvasContext} from "./context/CanvasContext.tsx";
import {LatchNode} from "./components/hw/LatchNode.tsx";
import {CustomChipNode} from "./components/hw/CustomChipNode.tsx";

const nodeTypes: NodeTypes = {
    logicGate: LogicGateNode,
    inputPin: InputPinNode,
    outputPin: OutputPinNode,
    latch: LatchNode,
    customChip: CustomChipNode,
};

export default function App() {
    const {nodes, edges, onNodesChange, onEdgesChange, onConnect, addNode, clipboard, setClipboard, cloneNodes, clearNodes} = useCanvasContext();
    const {screenToFlowPosition, getNodes, getEdges, deleteElements, setNodes} = useReactFlow();
    const {runSimulation} = useCanvasContext();
    const {showContextMenu} = useContextMenu();
    const {showModal} = useModal();

    const currentMousePos = useRef<{ x: number, y: number } | null>(null);
    const isCanvasHovered = useRef(false);
    const menuOpenPos = useRef<{ x: number, y: number } | null>(null);

    const defaultEdgeOptions = {
        style: {strokeWidth: 2, transition: 'stroke 0.2s'},
    };

    const getStyledEdges = useMemo(() => {
        return edges.map(edge => {
            const sourceNode = nodes.find(n => n.id === edge.source);
            const sourceNodeData = sourceNode?.data as unknown as GenericNodeData;
            const isActive = sourceNodeData?.values?.[edge.sourceHandle || 'out'] === true;

            return {
                ...edge,
                style: {
                    stroke: isActive ? 'var(--color-success)' : 'var(--color-border)',
                    strokeWidth: isActive ? 3 : 2,
                    transition: 'stroke 0.2s, stroke-width 0.2s'
                }
            };
        });
    }, [edges, nodes]);

    useEffect(() => {
        const handleMouseMove = (e: MouseEvent) => {
            currentMousePos.current = {x: e.clientX, y: e.clientY};
        };

        window.addEventListener('mousemove', handleMouseMove);

        return () => window.removeEventListener('mousemove', handleMouseMove);
    }, []);

    const onDragOver = useCallback((event: DragEvent<HTMLDivElement>) => {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
    }, []);

    const onDrop = useCallback((event: DragEvent<HTMLDivElement>) => {
        event.preventDefault();
        const reactFlowDataStr = event.dataTransfer.getData('application/reactflow');
        if (!reactFlowDataStr) return;

        const template = JSON.parse(reactFlowDataStr);
        const position = screenToFlowPosition({x: event.clientX, y: event.clientY});

        const {internalComponents, values, ...cleanData} = template.data;

        const newNode: Node = {
            id: `node_${uuidv4()}`,
            type: template.type,
            position,
            origin: [0.5, 0.5] as [number, number],
            selected: true,
            data: {
                ...cleanData,
                values: {}
            }
        };
        addNode(newNode);
        runSimulation();
    }, [screenToFlowPosition, addNode]);


    const handleDelete = useCallback(() => {
        const selectedNodes = getNodes().filter(n => n.selected);
        const selectedEdges = getEdges().filter(e => e.selected);
        if (selectedNodes.length > 0 || selectedEdges.length > 0) {
            deleteElements({nodes: selectedNodes, edges: selectedEdges});
        }
    }, [getNodes, getEdges, deleteElements]);

    const handleCopy = useCallback(() => {
        const selectedNodes = getNodes().filter(n => n.selected);
        if (selectedNodes.length > 0) setClipboard(selectedNodes);
    }, [getNodes, setClipboard]);

    const handleCut = useCallback(() => {
        handleCopy();
        handleDelete();
    }, [handleCopy, handleDelete]);

    const handleDuplicate = useCallback(() => {
        const selectedNodes = getNodes().filter(n => n.selected);
        if (selectedNodes.length > 0) cloneNodes(selectedNodes);
    }, [getNodes, cloneNodes]);

    const handlePaste = useCallback((fromMenu: boolean = false) => {
        if (clipboard.length === 0) return;

        if (fromMenu && menuOpenPos.current) {
            const flowPos = screenToFlowPosition(menuOpenPos.current);
            cloneNodes(clipboard, flowPos);
        } else if (!fromMenu && isCanvasHovered.current && currentMousePos.current) {
            const flowPos = screenToFlowPosition(currentMousePos.current);
            cloneNodes(clipboard, flowPos);
        } else {
            cloneNodes(clipboard);
        }
    }, [clipboard, cloneNodes, screenToFlowPosition]);


    const onNodeContextMenu = useCallback((event: ReactMouseEvent, node: Node) => {
        event.preventDefault();

        menuOpenPos.current = {x: event.clientX, y: event.clientY};

        setNodes((currentNodes) => {
            const clickedNode = currentNodes.find((n) => n.id === node.id);
            if (clickedNode && !clickedNode.selected) {
                return currentNodes.map((n) => ({
                    ...n,
                    selected: n.id === node.id
                }));
            }
            return currentNodes;
        });

        showContextMenu({
            event: event,
            items: [
                {label: 'Cut', icon: <FaCut/>, onClick: handleCut},
                {label: 'Copy', icon: <FaCopy/>, onClick: handleCopy},
                {label: 'Duplicate', icon: <FaClone/>, onClick: handleDuplicate},
                {separator: true},
                {label: 'Delete', icon: <FaTrash/>, variant: 'error', onClick: handleDelete}
            ]
        });
    }, [setNodes, showContextMenu, handleCut, handleCopy, handleDuplicate, handleDelete]);

    const onPaneContextMenu = useCallback((event: ReactMouseEvent | MouseEvent) => {
        event.preventDefault();

        menuOpenPos.current = {x: (event as MouseEvent).clientX, y: (event as MouseEvent).clientY};

        showContextMenu({
            event: event as ReactMouseEvent,
            items: [
                {
                    label: 'Paste',
                    icon: <FaPaste/>,
                    onClick: () => handlePaste(true),
                    disabled: clipboard.length === 0
                }
            ]
        });
    }, [showContextMenu, handlePaste, clipboard.length]);


    const handleClearConfirmed = useCallback(() => {
        showModal({
            title: "Clear Canvas",
            message: "Are you sure you want to delete all components? This action cannot be undone.",
            type: "danger",
            confirmText: "Clear All",
            onConfirm: clearNodes
        });
    }, [showModal, clearNodes]);


    useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (document.activeElement?.tagName === 'INPUT' || document.activeElement?.tagName === 'TEXTAREA') return;

            if (event.key === 'Delete' || event.key === 'Backspace') handleDelete();
            if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'x') handleCut();
            if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'c') handleCopy();
            if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'v') {
                event.preventDefault();
                handlePaste(false);
            }
            if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'd') {
                event.preventDefault();
                handleDuplicate();
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [handleCopy, handleCut, handlePaste, handleDuplicate, handleDelete]);


    return (
        <div className={styles.appContainer}>
            <Header/>

            <main className={styles.mainWorkspace}>
                <Sidebar/>

                <section className={styles.canvasContainer}>
                    <ReactFlow
                        nodes={nodes}
                        edges={getStyledEdges}
                        defaultEdgeOptions={defaultEdgeOptions}
                        onNodesChange={onNodesChange}
                        onEdgesChange={onEdgesChange}
                        onConnect={onConnect}
                        nodeTypes={nodeTypes}
                        onDrop={onDrop}
                        onDragOver={onDragOver}
                        onNodeContextMenu={onNodeContextMenu}
                        onPaneContextMenu={onPaneContextMenu}
                        selectionOnDrag={true}
                        selectionMode={SelectionMode.Partial}
                        panOnDrag={[1, 2]}
                        multiSelectionKeyCode="Control"
                        fitView
                        colorMode="dark"
                        snapToGrid={true}
                    >
                        <Background variant={BackgroundVariant.Dots} gap={16} size={1} color="var(--color-border)"/>

                        <Controls>
                            <ControlButton
                                onClick={handleClearConfirmed}
                                title="Clear Canvas"
                                aria-label="Clear Canvas"
                            >
                                <FaTrash style={{color: 'white'}}/>
                            </ControlButton>
                        </Controls>

                        <MiniMap
                            nodeColor={(node) => {
                                if (node.data?.headerColor) return node.data.headerColor as string;
                                if (node.type === 'inputPin') return INPUT_COLOR;
                                if (node.type === 'outputPin') return OUTPUT_COLOR;
                                if (node.type === 'logicGate') return LOGIC_GATE_COLOR;
                                if (node.type === 'latch') return LATCH_COLOR;

                                return '#3e3e42';
                            }}
                            nodeBorderRadius={4}
                            maskColor="rgba(30, 30, 30, 0.7)"
                            style={{backgroundColor: 'var(--color-bg-panel)'}}
                        />
                    </ReactFlow>
                </section>
            </main>
        </div>
    );
}