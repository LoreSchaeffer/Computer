import {createContext, type PropsWithChildren, useContext, useEffect, useState} from 'react';
import type {ChipDefinition, HardwareTemplate} from "../types/hardware.ts";
import {loadDirectoryHandle, saveDirectoryHandle} from "../utils/IndexedDB.ts";
import {DEF_CHIP_COLOR, DEF_CHIP_GROUP, DEF_CHIP_NAME, STORAGE_KEY_STATE} from "../utils/consts.ts";

const IO_NODES: HardwareTemplate[] = [
    {type: 'inputPin', data: {typeLabel: 'INPUT', label: 'IN', group: 'I/O Nodes', inputs: [], outputs: ['Out']}},
    {type: 'inputPin', data: {typeLabel: 'INPUT4', label: 'IN4', group: 'I/O Nodes', inputs: [], outputs: ['Out0', 'Out1', 'Out2', 'Out3']}},
    {type: 'inputPin', data: {typeLabel: 'INPUT8', label: 'IN8', group: 'I/O Nodes', inputs: [], outputs: ['Out0', 'Out1', 'Out2', 'Out3', 'Out4', 'Out5', 'Out6', 'Out7']}},
    {type: 'inputPin', data: {typeLabel: 'INPUT16', label: 'IN16', group: 'I/O Nodes', inputs: [], outputs: ['Out0', 'Out1', 'Out2', 'Out3', 'Out4', 'Out5', 'Out6', 'Out7', 'Out8', 'Out9', 'Out10', 'Out11', 'Out12', 'Out13', 'Out14', 'Out15']}},
    {type: 'outputPin', data: {typeLabel: 'OUTPUT', label: 'OUT', group: 'I/O Nodes', inputs: ['In'], outputs: []}},
    {type: 'outputPin', data: {typeLabel: 'OUTPUT4', label: 'OUT4', group: 'I/O Nodes', inputs: ['In0', 'In1', 'In2', 'In3'], outputs: []}},
    {type: 'outputPin', data: {typeLabel: 'OUTPUT8', label: 'OUT8', group: 'I/O Nodes', inputs: ['In0', 'In1', 'In2', 'In3', 'In4', 'In5', 'In6', 'In7'], outputs: []}},
    {type: 'outputPin', data: {typeLabel: 'OUTPUT16', label: 'OUT16', group: 'I/O Nodes', inputs: ['In0', 'In1', 'In2', 'In3', 'In4', 'In5', 'In6', 'In7', 'In8', 'In9', 'In10', 'In11', 'In12', 'In13', 'In14', 'In15'], outputs: []}},
    {type: 'logicGate', data: {typeLabel: 'VCC', label: 'VCC', group: 'I/O Nodes', inputs: [], outputs: ['Out']}},
    {type: 'logicGate', data: {typeLabel: 'GND', label: 'GND', group: 'I/O Nodes', inputs: [], outputs: ['Out']}},
];

const GATE_NODES: HardwareTemplate[] = [
    {type: 'logicGate', data: {typeLabel: 'AndGate', label: 'AND', inputs: ['A', 'B'], outputs: ['Out'], group: 'Primitives'}},
    {type: 'logicGate', data: {typeLabel: 'NandGate', label: 'NAND', inputs: ['A', 'B'], outputs: ['Out'], group: 'Primitives'}},
    {type: 'logicGate', data: {typeLabel: 'OrGate', label: 'OR', inputs: ['A', 'B'], outputs: ['Out'], group: 'Primitives'}},
    {type: 'logicGate', data: {typeLabel: 'NorGate', label: 'NOR', inputs: ['A', 'B'], outputs: ['Out'], group: 'Primitives'}},
    {type: 'logicGate', data: {typeLabel: 'XorGate', label: 'XOR', inputs: ['A', 'B'], outputs: ['Out'], group: 'Primitives'}},
    {type: 'logicGate', data: {typeLabel: 'NotGate', label: 'NOT', inputs: ['In'], outputs: ['Out'], group: 'Primitives'}},
    {type: 'latch', data: {typeLabel: 'DLatch', label: 'D-Latch', inputs: ['D', 'En'], outputs: ['Q', '!Q'], group: 'Primitives'}}
];

interface WorkspaceContextType {
    chipName: string;
    chipColor: string;
    chipGroup: string;
    setChipName: (name: string) => void;
    setChipColor: (color: string) => void;
    setChipGroup: (group: string) => void;
    library: HardwareTemplate[];
    groups: string[];
    isWorkspaceConnected: boolean;
    workspaceHandle: any | null;
    connectWorkspace: (forcePicker?: boolean) => Promise<void>;
    isWorkspaceReady: boolean;
}

const WorkspaceContext = createContext<WorkspaceContextType | undefined>(undefined);

export function WorkspaceProvider({children}: PropsWithChildren) {
    const getInitialWorkspaceState = () => {
        try {
            const saved = localStorage.getItem(STORAGE_KEY_STATE);
            if (saved) {
                const parsed = JSON.parse(saved);
                return {
                    chipName: parsed.chipName || DEF_CHIP_NAME,
                    chipColor: parsed.chipColor || DEF_CHIP_COLOR,
                    chipGroup: parsed.chipGroup || DEF_CHIP_GROUP
                };
            }
        } catch (e) {
        }
        return {chipName: DEF_CHIP_NAME, chipColor: DEF_CHIP_COLOR, chipGroup: DEF_CHIP_GROUP};
    };

    const initialState = getInitialWorkspaceState();
    const [chipName, setChipName] = useState<string>(initialState.chipName);
    const [chipColor, setChipColor] = useState<string>(initialState.chipColor);
    const [chipGroup, setChipGroup] = useState<string>(initialState.chipGroup);

    const [library, setLibrary] = useState<HardwareTemplate[]>([...IO_NODES, ...GATE_NODES]);
    const [groups, setGroups] = useState<string[]>(Array.from(new Set([...IO_NODES, ...GATE_NODES].map(n => n.data.group!))).sort());
    const [isWorkspaceConnected, setIsWorkspaceConnected] = useState(false);
    const [workspaceHandle, setWorkspaceHandle] = useState<any | null>(null);
    const [isWorkspaceReady, setIsWorkspaceReady] = useState(false);

    const connectWorkspace = async (forcePicker = false) => {
        try {
            let handle = forcePicker ? null : await loadDirectoryHandle();
            if (!handle) {
                handle = await (window as any).showDirectoryPicker({mode: 'readwrite'});
                await saveDirectoryHandle(handle!);
            } else {
                const permission = await (handle as any).requestPermission({mode: 'readwrite'});
                if (permission !== 'granted') throw new Error("Permission denied");
            }
            setWorkspaceHandle(handle);

            const foundGroups = new Set<string>([...IO_NODES, ...GATE_NODES].map(n => n.data.group!));

            const rawJSONs: { json: ChipDefinition, groupName: string, fileHandle: any }[] = [];

            for await (const entry of (handle as any).values()) {
                if (entry.kind === 'file' && entry.name.endsWith('.json')) {
                    const file = await entry.getFile();
                    const text = await file.text();
                    try {
                        const json = JSON.parse(text) as ChipDefinition;
                        const groupName = json.chipGroup || 'Custom Chips';
                        foundGroups.add(groupName);
                        rawJSONs.push({json, groupName, fileHandle: entry});
                    } catch (err) {
                        console.warn("Failed to parse JSON:", entry.name);
                    }
                }
            }

            const tempTemplateMap = new Map<string, any>();
            [...IO_NODES, ...GATE_NODES].forEach(n => tempTemplateMap.set(n.data.typeLabel, n.data));
            rawJSONs.forEach(({json}) => {
                tempTemplateMap.set(json.chipName, {inputs: json.pins?.inputs || [], outputs: json.pins?.outputs || []});
            });

            const loadedChips: HardwareTemplate[] = [];

            for (const {json, groupName, fileHandle} of rawJSONs) {
                let needsMigration = false;

                const migratedComponents = (json.components || []).map((comp: any) => {
                    const templateDef = tempTemplateMap.get(comp.type);
                    const expectedIns = templateDef?.inputs || [];
                    const expectedOuts = templateDef?.outputs || [];

                    const newInputs: Record<string, string> = {};
                    const newOutputs: Record<string, string[]> = {};

                    if (Array.isArray(comp.inputs)) {
                        needsMigration = true;
                        comp.inputs.forEach((wire: string, i: number) => {
                            newInputs[expectedIns[i] || `unk_${i}`] = wire;
                        });
                    } else {
                        Object.assign(newInputs, comp.inputs);
                    }

                    if (Array.isArray(comp.outputs)) {
                        needsMigration = true;
                        comp.outputs.forEach((wire: string, i: number) => {
                            newOutputs[expectedOuts[i] || `unk_${i}`] = [wire];
                        });
                    } else {
                        Object.entries(comp.outputs || {}).forEach(([k, v]) => {
                            if (typeof v === 'string') {
                                needsMigration = true;
                                newOutputs[k] = [v];
                            } else {
                                newOutputs[k] = v as string[];
                            }
                        });
                    }

                    return {...comp, inputs: newInputs, outputs: newOutputs};
                });

                if (needsMigration) {
                    console.log(`Aggiornamento automatico su disco eseguito per il chip: ${json.chipName}.json`);
                    json.components = migratedComponents;
                    try {
                        const writable = await fileHandle.createWritable();
                        await writable.write(JSON.stringify(json, null, 2));
                        await writable.close();
                    } catch (e) {
                        console.error("Errore durante l'auto-salvataggio della migrazione:", e);
                    }
                }

                loadedChips.push({
                    type: 'customChip',
                    data: {
                        typeLabel: json.chipName,
                        label: json.chipName,
                        inputs: json.pins?.inputs || [],
                        outputs: json.pins?.outputs || [],
                        headerColor: json.chipColor || '#1e3799',
                        group: groupName,
                        internalComponents: migratedComponents
                    }
                });
            }

            setGroups(Array.from(foundGroups).sort());
            setLibrary([...IO_NODES, ...GATE_NODES, ...loadedChips.sort((a, b) => a.data.label.localeCompare(b.data.label))]);
            setIsWorkspaceConnected(true);
        } catch (error) {
            console.error("Workspace connection failed:", error);
            setLibrary([...IO_NODES, ...GATE_NODES]);
        }
    };

    useEffect(() => {
        connectWorkspace()
            .catch(() => setLibrary([...IO_NODES, ...GATE_NODES]))
            .finally(() => setIsWorkspaceReady(true));
    }, []);

    return (
        <WorkspaceContext.Provider value={{
            chipName,
            chipColor,
            chipGroup,
            setChipName,
            setChipColor,
            setChipGroup,
            library,
            groups,
            isWorkspaceConnected,
            workspaceHandle,
            connectWorkspace,
            isWorkspaceReady
        }}>
            {children}
        </WorkspaceContext.Provider>
    );
}

export const useWorkspaceContext = () => {
    const ctx = useContext(WorkspaceContext);
    if (!ctx) throw new Error("useWorkspaceContext must be used within WorkspaceProvider");
    return ctx;
};