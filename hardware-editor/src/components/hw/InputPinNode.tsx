import type {NodeProps} from "@xyflow/react";
import {GenericNode, type GenericNodeData} from "./GenericNode.tsx";
import {INPUT_COLOR} from "../../utils/consts.ts";
import {useCanvasContext} from "../../context/CanvasContext.tsx";

export function InputPinNode(props: NodeProps) {
    const {toggleInput} = useCanvasContext();

    const nodeData = props.data as unknown as GenericNodeData;
    const isActive = nodeData.values?.['out'] === true;

    const dataWithConfig: GenericNodeData = {
        ...(props.data as unknown as GenericNodeData),
        typeLabel: 'INPUT',
        headerColor: INPUT_COLOR,
        inputs: [],
        outputs: ['out'],
        customControl: (
            <button
                className="nodrag"
                onClick={() => toggleInput(props.id)}
                style={{
                    width: '24px', height: '24px', borderRadius: '50%',
                    backgroundColor: isActive ? 'var(--color-success)' : 'var(--color-bg-primary)',
                    border: `1px solid ${isActive ? 'white' : 'var(--color-border)'}`,
                    cursor: 'pointer', transition: 'all 0.2s ease',
                    margin: '0 auto'
                }}
                title="Toggle Input"
            />
        )
    };

    return <GenericNode {...props} data={dataWithConfig}/>;
}