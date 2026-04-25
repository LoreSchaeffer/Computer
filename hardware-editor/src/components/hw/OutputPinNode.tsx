import type {NodeProps} from "@xyflow/react";
import {GenericNode, type GenericNodeData} from "./GenericNode.tsx";
import {OUTPUT_COLOR} from "../../utils/consts.ts";

export function OutputPinNode(props: NodeProps) {
    const dataWithConfig: GenericNodeData = {
        ...(props.data as unknown as GenericNodeData),
        typeLabel: 'OUTPUT',
        headerColor: OUTPUT_COLOR,
        inputs: ['in'],
        outputs: []
    };

    return <GenericNode {...props} data={dataWithConfig}/>;
}