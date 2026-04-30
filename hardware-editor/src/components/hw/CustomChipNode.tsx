import type {NodeProps} from "@xyflow/react";
import {type AppNode, GenericNode} from "./GenericNode.tsx";

export function CustomChipNode(props: NodeProps<AppNode>) {
    return <GenericNode {...props} />;
}