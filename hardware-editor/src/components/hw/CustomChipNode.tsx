import type {NodeProps} from "@xyflow/react";
import {GenericNode, type GenericNodeData} from "./GenericNode.tsx";

export function CustomChipNode(props: NodeProps) {
    const dataWithStyles: GenericNodeData = {
        ...(props.data as unknown as GenericNodeData),
    };

    return <GenericNode {...props} data={dataWithStyles}/>;
}