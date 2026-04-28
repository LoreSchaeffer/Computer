import styles from './GenericNode.module.css';
import {type KeyboardEvent, type ReactNode, useEffect, useRef, useState} from 'react';
import {Handle, type Node, type NodeProps, Position} from '@xyflow/react';
import clsx from 'clsx';
import {useCanvasContext} from "../../context/CanvasContext.tsx";

export interface GenericNodeData extends Record<string, unknown> {
    label: string;
    typeLabel: string;
    inputs?: string[];
    outputs?: string[];
    headerColor?: string;
    values?: Record<string, boolean>;
    internalState?: Record<string, any>;
    customControl?: ReactNode;
}

export type AppNode = Node<GenericNodeData>;

export function GenericNode({id, data, selected}: NodeProps<AppNode>) {
    const {updateCustomNodeData} = useCanvasContext();

    const [isEditing, setIsEditing] = useState<boolean>(false);
    const [editValue, setEditValue] = useState<string>(data.label);

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
            setEditValue(data.label);
            setIsEditing(false);
        }
    };

    const maxPins = Math.max(data.inputs?.length || 0, data.outputs?.length || 0);
    const dynamicMinHeight = Math.max(40, maxPins * 20 + 20);

    return (
        <div className={clsx(styles.nodeWrapper, selected && styles.selected)}>
            <div
                className={styles.header}
                style={{backgroundColor: data.headerColor || 'transparent'}}
                onDoubleClick={handleDoubleClick}
            >
                <div className={styles.titleContainer}>
                    <div className={clsx(styles.titleText, isEditing && styles.titleTextHidden)}>
                        {isEditing ? (editValue || '\u00A0') : data.label}
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

                <div className={styles.subtitle}>{data.typeLabel}</div>
            </div>

            <div className={styles.body} style={{minHeight: `${dynamicMinHeight}px`}}>
                {data.customControl && (
                    <div className={styles.customControlContainer}>
                        {data.customControl}
                    </div>
                )}

                {data.inputs && data.inputs.map((pinId, index) => {
                    const topPercent = ((index + 1) / (data.inputs!.length + 1)) * 100;
                    return (
                        <div key={`in-${pinId}`}>
                            <Handle
                                className={data.values?.[pinId] ? styles.handleActive : ''}
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

                {data.outputs && data.outputs.map((pinId, index) => {
                    const topPercent = ((index + 1) / (data.outputs!.length + 1)) * 100;
                    return (
                        <div key={`out-${pinId}`}>
                            <Handle
                                className={data.values?.[pinId] ? styles.handleActive : ''}
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