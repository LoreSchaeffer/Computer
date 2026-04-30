import styles from './IONode.module.css';
import {type AppNode, GenericNode, type GenericNodeData} from "./GenericNode.tsx";
import {OUTPUT_COLOR} from "../../utils/consts.ts";
import type {NodeProps} from "@xyflow/react";
import {clsx} from "clsx";

export function OutputPinNode(props: NodeProps<AppNode>) {
    const dataWithConfig: GenericNodeData = {
        ...(props.data as unknown as GenericNodeData),
        typeLabel: 'OUTPUT',
        headerColor: OUTPUT_COLOR,
        customControl: (
            <>
                {props.data.inputs && props.data.inputs.map((input, index) => {
                    const toPercent = ((index + 1) / (props.data.inputs!.length + 1)) * 100;
                    const isActive = props.data.values?.[input] === true;

                    return (
                        <div
                            key={input}
                            className={clsx(styles.status, isActive && styles.active)}
                            title={`Input ${input} is ${isActive ? 'ON' : 'OFF'}`}
                            style={{
                                top: `${toPercent}%`
                            }}
                        />
                    )
                })}
            </>
        )
    };

    return <GenericNode {...props} data={dataWithConfig}/>;
}