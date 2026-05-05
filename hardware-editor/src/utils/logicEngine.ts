import type {HardwareTemplate} from "../types/hardware.ts";

export const GATE_LOGIC: Record<string, (inputs: boolean[], previousState?: boolean) => boolean> = {
    'AndGate': (ins) => ins.length > 0 && ins.every(v => v),
    'OrGate': (ins) => ins.some(v => v),
    'NotGate': (ins) => !ins[0],
    'NandGate': (ins) => !(ins.length > 0 && ins.every(v => v)),
    'NorGate': (ins) => !ins.some(v => v),
    'XorGate': (ins) => ins.filter(v => v).length % 2 !== 0,
    'VCC': () => true,
    'GND': () => false,
};

export const SEQUENTIAL_LOGIC: Record<string, (inputs: boolean[], currentState: any) => Record<string, boolean>> = {
    'DLatch': (ins, currentState) => {
        const data = ins[0];
        const enable = ins[1];
        const latchedValue = enable ? data : (currentState?.values?.['Q'] || false);
        return {'Q': latchedValue, '!Q': !latchedValue};
    },
    'DFlipFlop': (ins, currentState) => {
        const data: boolean = ins[0];
        const clock: boolean = ins[1];
        const enable: boolean = ins[2];

        const prevClock: boolean = currentState?.values?.['prevClock'] || false;
        const currentQ: boolean = currentState?.values?.['Q'] || false;
        const isRisingEdge: boolean = clock && !prevClock;

        const nextQ: boolean = (isRisingEdge && enable) ? data : currentQ;

        return {
            'Q': nextQ,
            '!Q': !nextQ,
            'prevClock': clock
        };
    }
};

export const getExpectedOutputs = (typeLabel: string, library: HardwareTemplate[]): string[] => {
    const target = typeLabel.trim();

    if (GATE_LOGIC[target]) return ['Out'];
    if (SEQUENTIAL_LOGIC[target]) return ['Q', '!Q'];

    const chip = library.find(c =>
        c.data.typeLabel?.trim().toLowerCase() === target.toLowerCase() ||
        c.data.label?.trim().toLowerCase() === target.toLowerCase()
    );
    return chip?.data.outputs || [];
};