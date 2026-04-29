import styles from '../components/ui/Modal.module.css';
import {createContext, type ReactNode, useContext, useEffect, useState} from 'react';
import {Button} from '../components/ui/forms/Button.tsx';

interface ModalConfig {
    title: string;
    message: string;
    type?: 'info' | 'warning' | 'danger';
    confirmText?: string;
    cancelText?: string;
    onConfirm?: () => void;
    onCancel?: () => void;
}

interface ModalContextType {
    showModal: (config: ModalConfig) => void;
    hideModal: () => void;
}

const ModalContext = createContext<ModalContextType | undefined>(undefined);

export function ModalProvider({children}: { children: ReactNode }) {
    const [modalConfig, setModalConfig] = useState<ModalConfig | null>(null);

    const showModal = (config: ModalConfig) => setModalConfig(config);
    const hideModal = () => setModalConfig(null);

    const handleConfirm = () => {
        if (modalConfig?.onConfirm) modalConfig.onConfirm();
        hideModal();
    };

    const handleCancel = () => {
        if (modalConfig?.onCancel) modalConfig.onCancel();
        hideModal();
    };

    useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (!modalConfig) return;

            if (event.key === 'Enter') {
                event.preventDefault();
                handleConfirm();
            } else if (event.key === 'Escape') {
                event.preventDefault();
                handleCancel();
            }
        };

        if (modalConfig) window.addEventListener('keydown', handleKeyDown);

        return () => {
            window.removeEventListener('keydown', handleKeyDown);
        };
    }, [modalConfig]);

    return (
        <ModalContext.Provider value={{showModal, hideModal}}>
            {children}
            {modalConfig && (
                <div
                    className={styles.modalOverlay}
                    onClick={handleCancel}
                >
                    <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                        <h3 className={styles.modalTitle}>{modalConfig.title}</h3>
                        <p className={styles.modalMessage}>{modalConfig.message}</p>
                        <div className={styles.modalActions}>
                            <Button variant="secondary" onClick={handleCancel}>
                                {modalConfig.cancelText || 'Cancel'}
                            </Button>
                            <Button
                                variant={modalConfig.type === 'danger' ? 'error' : 'primary'}
                                onClick={handleConfirm}
                            >
                                {modalConfig.confirmText || 'Confirm'}
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        </ModalContext.Provider>
    );
}

export const useModal = () => {
    const context = useContext(ModalContext);
    if (!context) throw new Error("useModal must be used within a ModalProvider");
    return context;
};