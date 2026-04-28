import type {NodeProps} from "@xyflow/react";
import {type AppNode, GenericNode} from "./GenericNode.tsx";
import {LOGIC_GATE_COLOR} from "../../utils/consts.ts";

export function LogicGateNode(props: NodeProps<AppNode>) {
    return <GenericNode {...props} data={{...props.data, headerColor: LOGIC_GATE_COLOR}}/>;
}