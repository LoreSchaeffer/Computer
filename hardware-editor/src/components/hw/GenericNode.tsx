import styles from './GenericNode.module.css';
import {type KeyboardEvent, type ReactNode, useEffect, useRef} from 'react';
import {useState} from 'react';
import {Handle, type NodeProps, Position} from '@xyflow/react';
import clsx from 'clsx';
import {useCanvasContext} from "../../context/CanvasContext.tsx";

export interface GenericNodeData extends Record<string, unknown> {
    label: string;
    typeLabel: string;
    inputs?: string[];
    outputs?: string[];
    headerColor?: string;
    values?: Record<string, boolean>;
    customControl?: ReactNode;
    internalComponents?: any[];
}

export function GenericNode({id, data, selected}: NodeProps) {
    const nodeData = data as unknown as GenericNodeData;
    const {updateCustomNodeData} = useCanvasContext();

    const [isEditing, setIsEditing] = useState<boolean>(false);
    const [editValue, setEditValue] = useState<string>(nodeData.label);

    const inputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (isEditing) {
            inputRef.current?.focus();
            inputRef.current?.select();
        }
    }, [isEditing]);

    const handleDoubleClick = () => setIsEditing(true);

    const commitNameChange = () => {
        setIsEditing(false);
        const newLabel = editValue.trim() !== '' ? editValue.trim() : 'UNNAMED';
        setEditValue(newLabel);
        updateCustomNodeData(id, {label: newLabel});
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') commitNameChange();
        if (e.key === 'Escape') {
            setEditValue(nodeData.label);
            setIsEditing(false);
        }
    };

    const maxPins = Math.max(nodeData.inputs?.length || 0, nodeData.outputs?.length || 0);
    const dynamicMinHeight = Math.max(40, maxPins * 20 + 20);

    return (
        <div className={clsx(styles.nodeWrapper, selected && styles.selected)}>
            <div
                className={styles.header}
                style={{backgroundColor: nodeData.headerColor || 'transparent'}}
                onDoubleClick={handleDoubleClick}
            >
                <div className={styles.titleContainer}>
                    <div className={clsx(styles.titleText, isEditing && styles.titleTextHidden)}>
                        {isEditing ? (editValue || '\u00A0') : nodeData.label}
                    </div>

                    {isEditing && (
                        <input
                            ref={inputRef}
                            autoFocus
                            size={1}
                            className={styles.titleInput}
                            value={editValue}
                            onChange={(e) => setEditValue(e.target.value)}
                            onBlur={commitNameChange}
                            onKeyDown={handleKeyDown}
                        />
                    )}
                </div>

                <div className={styles.subtitle}>{nodeData.typeLabel}</div>
            </div>

            <div className={styles.body} style={{minHeight: `${dynamicMinHeight}px`}}>
                {nodeData.customControl && (
                    <div className={styles.customControlContainer}>
                        {nodeData.customControl}
                    </div>
                )}

                {nodeData.inputs && nodeData.inputs.map((pinId, index) => {
                    const topPercent = ((index + 1) / (nodeData.inputs!.length + 1)) * 100;
                    return (
                        <div key={`in-${pinId}`}>
                            <Handle
                                className={nodeData.values?.[pinId] ? styles.handleActive : ''}
                                type="target"
                                position={Position.Left}
                                id={pinId}
                                style={{top: `${topPercent}%`}}
                            />
                            <span className={clsx(styles.pinLabel, styles.pinLabelLeft)} style={{top: `${topPercent}%`}}>
                                {pinId}
                            </span>
                        </div>
                    );
                })}

                {nodeData.outputs && nodeData.outputs.map((pinId, index) => {
                    const topPercent = ((index + 1) / (nodeData.outputs!.length + 1)) * 100;
                    return (
                        <div key={`out-${pinId}`}>
                            <Handle
                                className={nodeData.values?.[pinId] ? styles.handleActive : ''}
                                type="source"
                                position={Position.Right}
                                id={pinId}
                                style={{top: `${topPercent}%`}}
                            />
                            <span className={clsx(styles.pinLabel, styles.pinLabelRight)} style={{top: `${topPercent}%`}}>
                                {pinId}
                            </span>
                        </div>
                    );
                })}

            </div>
        </div>
    );
}