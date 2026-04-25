import type {NodeProps} from "@xyflow/react";
import {GenericNode, type GenericNodeData} from "./GenericNode.tsx";
import {LOGIC_GATE_COLOR} from "../../utils/consts.ts";

export function LogicGateNode(props: NodeProps) {
    const dataWithStyles: GenericNodeData = {
        ...(props.data as unknown as GenericNodeData),
        headerColor: LOGIC_GATE_COLOR,
    };

    return <GenericNode {...props} data={dataWithStyles}/>;
}