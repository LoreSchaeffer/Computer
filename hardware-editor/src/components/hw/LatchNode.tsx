import type {NodeProps} from "@xyflow/react";
import {GenericNode, type GenericNodeData} from "./GenericNode.tsx";
import {LATCH_COLOR} from "../../utils/consts.ts";

export function LatchNode(props: NodeProps) {
    const dataWithStyles: GenericNodeData = {
        ...(props.data as unknown as GenericNodeData),
        headerColor: LATCH_COLOR,
    };

    return <GenericNode {...props} data={dataWithStyles}/>;
}