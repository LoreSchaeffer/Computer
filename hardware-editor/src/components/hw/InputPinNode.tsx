import styles from './IONode.module.css';
import type {NodeProps} from "@xyflow/react";
import {type AppNode, GenericNode, type GenericNodeData} from "./GenericNode.tsx";
import {INPUT_COLOR} from "../../utils/consts.ts";
import {useCanvasContext} from "../../context/CanvasContext.tsx";
import {clsx} from "clsx";

export function InputPinNode(props: NodeProps<AppNode>) {
    const {toggleInput} = useCanvasContext();

    const dataWithConfig: GenericNodeData = {
        ...(props.data as unknown as GenericNodeData),
        headerColor: INPUT_COLOR,
        customControl: (
            <>
                {
                    props.data.outputs && props.data.outputs.map((output, index) => {
                        const topPercent = ((index + 1) / (props.data.outputs!.length + 1)) * 100;

                        return (
                            <button
                                key={output}
                                className={clsx('nodrag', styles.status, styles.input, props.data.values?.[output] && styles.active)}
                                onClick={() => toggleInput(props.id, output)}
                                title={`Toggle ${output}`}
                                style={{
                                    top: `${topPercent}%`
                                }}
                            />
                        )
                    })
                }
            </>
        )
    };

    return <GenericNode {...props} data={dataWithConfig}/>;
}