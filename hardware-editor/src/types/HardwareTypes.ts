export interface PinsDefinition {
    inputs: string[];
    outputs: string[];
}

export interface ComponentDefinition {
    type: string;
    name: string;
    inputs: string[];
    outputs: string[];
}

export interface ChipDefinition {
    chipName: string;
    chipColor?: string;
    group?: string;
    pins: PinsDefinition;
    internalWires: string[];
    components: ComponentDefinition[];
}

export interface HardwareTemplate {
    type: string;
    data: {
        typeLabel: string;
        label: string;
        inputs?: string[];
        outputs?: string[];
        headerColor?: string;
        group?: string;
        internalComponents?: any[];
    };
}