import type {NodeProps} from "@xyflow/react";
import {GenericNode, type GenericNodeData} from "./GenericNode.tsx";
import {OUTPUT_COLOR} from "../../utils/consts.ts";

export function OutputPinNode(props: NodeProps) {
    const nodeData = props.data as unknown as GenericNodeData;
    const isActive = nodeData.values?.['In'] === true;

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