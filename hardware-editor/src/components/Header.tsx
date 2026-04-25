import styles from './Header.module.css';
import {Button} from "./ui/forms/Button.tsx";
import {FaFolderOpen, FaPen, FaSave} from "react-icons/fa";
import {type KeyboardEvent, useEffect, useState} from "react";
import {FaFile, FaFolderTree} from "react-icons/fa6";
import Input from "./ui/forms/Input.tsx";
import clsx from "clsx";
import {useModal} from "../context/ModalContext.tsx";
import {useWorkspaceContext} from "../context/WorkspaceContext.tsx";
import {useProjectManager} from "./hooks/useProjectManager.ts";
import {PRESET_COLORS} from "../utils/consts.ts";

export default function Header() {
    const {chipName, setChipName, chipGroup, setChipGroup, chipColor, setChipColor, groups} = useWorkspaceContext();
    const {newChip, openChip, saveChip} = useProjectManager();
    const {showModal} = useModal();

    const [isEditingName, setIsEditingName] = useState<boolean>(false);
    const [editNameValue, setEditNameValue] = useState<string>(chipName);

    useEffect(() => {
        setEditNameValue(chipName);
    }, [chipName]);

    const startEditing = () => {
        setEditNameValue(chipName);
        setIsEditingName(true);
    };

    const commitNameChange = () => {
        setIsEditingName(false);
        const newName = editNameValue.trim() !== '' ? editNameValue.trim() : 'UntitledChip';
        setChipName(newName);
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') commitNameChange();
        if (e.key === 'Escape') {
            setEditNameValue(chipName);
            setIsEditingName(false);
        }
    };

    const handleNewChipClick = () => {
        showModal({
            title: "Create New Chip",
            message: "Are you sure? All unsaved progress on the canvas will be lost.",
            type: "danger",
            confirmText: "Create New",
            onConfirm: newChip
        });
    };

    const handleSaveClick = async () => {
        const result = await saveChip();
        if (!result.success && result.error) {
            showModal({
                title: "Validation Error",
                message: result.error,
                type: "warning",
                confirmText: "Got it"
            });
        }
    };

    const handleOpenClick = async () => {
        const result = await openChip();
        if (!result.success && result.error) {
            showModal({
                title: "Error Opening File",
                message: result.error,
                type: "warning"
            });
        }
    };

    useEffect(() => {
        const handleGlobalKeyDown = (e: globalThis.KeyboardEvent) => {
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
                e.preventDefault();
                handleSaveClick();
            }
        };
        window.addEventListener('keydown', handleGlobalKeyDown);
        return () => window.removeEventListener('keydown', handleGlobalKeyDown);
    }, [saveChip]);

    return (
        <header className={styles.header}>
            <div className={styles.leftSection}>

                <div className={styles.fieldGroup}>
                    <span className={styles.fieldLabel}>Chip Name</span>
                    {isEditingName ? (
                        <div className={styles.titleInputWrapper}>
                            <Input
                                autoFocus
                                background={'secondary'}
                                value={editNameValue}
                                onChange={(e) => setEditNameValue(e.target.value)}
                                onBlur={commitNameChange}
                                onKeyDown={handleKeyDown}
                            />
                        </div>
                    ) : (
                        <div className={styles.titleText} onDoubleClick={startEditing} title="Double click to edit">
                            {chipName}
                            <FaPen style={{fontSize: '0.7rem', opacity: 0.5, marginLeft: '6px'}}/>
                        </div>
                    )}
                </div>

                <div className={styles.divider}/>

                <div className={styles.fieldGroup}>
                    <span className={styles.fieldLabel}>Group</span>
                    <div className={styles.groupInputWrapper}>
                        <Input
                            list="group-options"
                            background={'secondary'}
                            value={chipGroup}
                            onChange={(e) => setChipGroup(e.target.value)}
                            placeholder="Select or type..."
                            icon={<FaFolderTree/>}
                        />
                        <datalist id="group-options">
                            {groups.map(g => <option key={g} value={g}/>)}
                        </datalist>
                    </div>
                </div>

                <div className={styles.divider}/>

                <div className={styles.fieldGroup}>
                    <span className={styles.fieldLabel}>Color</span>
                    <div className={styles.colorControls}>
                        <div className={styles.presetColors}>
                            {PRESET_COLORS.map(color => (
                                <div
                                    key={color}
                                    className={clsx(styles.presetSwatch, chipColor === color && styles.active)}
                                    style={{backgroundColor: color}}
                                    onClick={() => setChipColor(color)}
                                    title={color}
                                />
                            ))}
                        </div>

                        <input
                            type="color"
                            className={styles.colorPicker}
                            value={chipColor}
                            onChange={(e) => setChipColor(e.target.value)}
                            title="Custom color"
                        />
                    </div>
                </div>

            </div>

            <div className={styles.actions}>
                <Button className={styles.square} variant="secondary" icon={<FaFile/>} onClick={handleNewChipClick} title="New Chip"/>
                <Button className={styles.square} variant="secondary" icon={<FaFolderOpen/>} onClick={handleOpenClick} title="Open File"/>
                <Button className={styles.square} variant="primary" icon={<FaSave/>} onClick={handleSaveClick} title="Save (Ctrl+S)"/>
            </div>
        </header>
    );
}