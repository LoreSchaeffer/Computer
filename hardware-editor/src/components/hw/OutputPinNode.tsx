import {type AppNode, GenericNode, type GenericNodeData} from "./GenericNode.tsx";
import {OUTPUT_COLOR} from "../../utils/consts.ts";
import type {NodeProps} from "@xyflow/react";

export function OutputPinNode(props: NodeProps<AppNode>) {
    const isActive = props.data.values?.['In'] === true;

    const dataWithConfig: GenericNodeData = {
        ...(props.data as unknown as GenericNodeData),
        typeLabel: 'OUTPUT',
        headerColor: OUTPUT_COLOR,
        inputs: ['In'],
        outputs: [],
        customControl: (
            <div
                style={{
                    width: '20px', height: '20px', borderRadius: '50%',
                    backgroundColor: isActive ? 'var(--color-success)' : 'var(--color-bg-primary)',
                    border: `1px solid ${isActive ? 'white' : 'var(--color-border)'}`,
                    transition: 'all 0.2s ease',
                    margin: '0 auto'
                }}
                title={isActive ? "HIGH" : "LOW"}
            />
        )
    };

    return <GenericNode {...props} data={dataWithConfig}/>;
}