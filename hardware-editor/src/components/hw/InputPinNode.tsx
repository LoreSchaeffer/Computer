import type {NodeProps} from "@xyflow/react";
import {GenericNode, type GenericNodeData} from "./GenericNode.tsx";
import {INPUT_COLOR} from "../../utils/consts.ts";

export function InputPinNode(props: NodeProps) {
    const dataWithConfig: GenericNodeData = {
        ...(props.data as unknown as GenericNodeData),
        typeLabel: 'INPUT',
        headerColor: INPUT_COLOR,
        inputs: [],
        outputs: ['out']
    };

    return <GenericNode {...props} data={dataWithConfig}/>;
}