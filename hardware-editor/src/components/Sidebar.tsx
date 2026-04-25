import styles from "./Sidebar.module.css";
import {type CSSProperties, type DragEvent, useMemo, useState} from 'react';
import type {HardwareTemplate} from "../types/HardwareTypes.ts";
import {Button} from "./ui/forms/Button.tsx";
import {FaFolder, FaFolderOpen} from "react-icons/fa6";
import {FaSearch} from "react-icons/fa";
import Input from "./ui/forms/Input.tsx";
import {useWorkspaceContext} from "../context/WorkspaceContext.tsx";
import {INPUT_COLOR, LATCH_COLOR, LOGIC_GATE_COLOR, OUTPUT_COLOR} from "../utils/consts.ts";

export default function Sidebar() {
    const {library, isWorkspaceConnected, connectWorkspace} = useWorkspaceContext();
    const [searchTerm, setSearchTerm] = useState('');

    const onDragStart = (event: DragEvent<HTMLDivElement>, template: HardwareTemplate) => {
        event.dataTransfer.setData('application/reactflow', JSON.stringify(template));
        event.dataTransfer.effectAllowed = 'move';
    };

    const groupedLibrary = useMemo(() => {
        const filtered = library.filter(comp =>
            comp.data.label.toLowerCase().includes(searchTerm.toLowerCase()) ||
            comp.data.typeLabel.toLowerCase().includes(searchTerm.toLowerCase())
        );

        const groups = new Map<string, HardwareTemplate[]>();
        filtered.forEach(comp => {
            const groupName = comp.data.group || 'Uncategorized';
            if (!groups.has(groupName)) groups.set(groupName, []);
            groups.get(groupName)!.push(comp);
        });

        const sortedGroups = Array.from(groups.entries()).sort((a, b) => {
            const groupA = a[0];
            const groupB = b[0];

            if (groupA === 'I/O Nodes') return -1;
            if (groupB === 'I/O Nodes') return 1;
            if (groupA === 'Logic Gates') return -1;
            if (groupB === 'Logic Gates') return 1;
            return groupA.localeCompare(groupB);
        });

        return new Map(sortedGroups);
    }, [library, searchTerm]);

    return (
        <aside className={styles.sidebar}>
            <div className={styles.workspaceSection}>
                <Button
                    variant={isWorkspaceConnected ? "secondary" : "primary"}
                    className={isWorkspaceConnected ? styles.workspaceBtnActive : styles.workspaceBtn}
                    onClick={() => connectWorkspace(true)}
                    icon={isWorkspaceConnected ? <FaFolderOpen/> : <FaFolder/>}
                >
                    {isWorkspaceConnected ? 'Workspace Linked' : 'Open Workspace Folder'}
                </Button>
            </div>

            <div className={styles.searchSection}>
                <Input
                    background={"secondary"}
                    type="text"
                    placeholder="Search components..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    icon={<FaSearch/>}
                />
            </div>

            <div className={styles.scrollArea}>
                {Array.from(groupedLibrary.entries()).map(([groupName, items]) => (
                    <div key={groupName} className={styles.section}>
                        <h3 className={styles.sectionTitle}>{groupName}</h3>
                        <div className={styles.componentGrid}>
                            {items.map((comp) => {
                                const getColor = (comp: HardwareTemplate) => {
                                    if (comp.data?.headerColor) return comp.data.headerColor as string;
                                    if (comp.type === 'inputPin') return INPUT_COLOR;
                                    if (comp.type === 'outputPin') return OUTPUT_COLOR;
                                    if (comp.type === 'logicGate') return LOGIC_GATE_COLOR;
                                    if (comp.type === 'latch') return LATCH_COLOR;
                                    return 'var(--color-primary)'
                                }

                                const color = getColor(comp);

                                return (
                                    <div
                                        key={comp.data.typeLabel}
                                        className={styles.dragItem}
                                        style={{'--chip-color': color} as CSSProperties}
                                        draggable
                                        onDragStart={(e) => onDragStart(e, comp)}
                                        title={comp.data.typeLabel}
                                    >
                                        {comp.data.label}
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>

        </aside>
    );
}