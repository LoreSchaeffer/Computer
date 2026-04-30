export interface PinsDefinition {
    inputs: string[];
    outputs: string[];
}

export interface ComponentDefinition {
    type: string;
    name: string;
    inputs: Record<string, string>;
    outputs: Record<string, string[]>;
}

export interface ChipDefinition {
    chipName: string;
    chipColor?: string;
    chipGroup?: string;
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
        internalComponents?: ComponentDefinition[];
    };
}