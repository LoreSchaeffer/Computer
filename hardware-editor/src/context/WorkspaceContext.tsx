import {createContext, type PropsWithChildren, useContext, useEffect, useState} from 'react';
import type {ChipDefinition, HardwareTemplate} from "../types/HardwareTypes.ts";
import {loadDirectoryHandle, saveDirectoryHandle} from "../utils/IndexedDB.ts";
import {DEF_CHIP_COLOR, DEF_CHIP_GROUP, DEF_CHIP_NAME, STORAGE_KEY_STATE} from "../utils/consts.ts";

const IO_NODES: HardwareTemplate[] = [
    {type: 'inputPin', data: {typeLabel: 'INPUT', label: 'IN', group: 'I/O Nodes', inputs: [], outputs: ['Out']}},
    {type: 'outputPin', data: {typeLabel: 'OUTPUT', label: 'OUT', group: 'I/O Nodes', inputs: ['In'], outputs: []}}
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

    const [library, setLibrary] = useState<HardwareTemplate[]>([]);
    const [groups, setGroups] = useState<string[]>(['Custom Chips']);
    const [isWorkspaceConnected, setIsWorkspaceConnected] = useState(false);
    const [workspaceHandle, setWorkspaceHandle] = useState<any | null>(null);

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

            const loadedChips: HardwareTemplate[] = [];
            const foundGroups = new Set<string>(['Primitives']);

            for await (const entry of (handle as any).values()) {
                if (entry.kind === 'file' && entry.name.endsWith('.json')) {
                    const file = await entry.getFile();
                    const text = await file.text();
                    try {
                        const json = JSON.parse(text) as ChipDefinition;
                        const groupName = json.group || 'Custom Chips';
                        foundGroups.add(groupName);
                        loadedChips.push({
                            type: 'gate',
                            data: {
                                typeLabel: json.chipName, label: json.chipName,
                                inputs: json.pins?.inputs || [], outputs: json.pins?.outputs || [],
                                headerColor: json.chipColor || '#1e3799', group: groupName
                            }
                        });
                    } catch (err) {
                        console.warn("Failed to parse JSON:", entry.name);
                    }
                }
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
        connectWorkspace().catch(() => setLibrary([...IO_NODES, ...GATE_NODES]));
    }, []);

    return (
        <WorkspaceContext.Provider value={{
            chipName, chipColor, chipGroup, setChipName, setChipColor, setChipGroup,
            library, groups, isWorkspaceConnected, workspaceHandle, connectWorkspace
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