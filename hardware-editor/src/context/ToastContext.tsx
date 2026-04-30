import styles from '../components/Toast.module.css';
import {createContext, type ReactNode, useCallback, useContext, useState} from 'react';
import {v4 as uuidv4} from 'uuid';
import {FaCheckCircle, FaExclamationCircle, FaExclamationTriangle, FaInfoCircle, FaTimes} from 'react-icons/fa';
import clsx from 'clsx';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

interface ToastMessage {
    id: string;
    type: ToastType;
    title: string;
    message?: string;
    duration?: number;
    isClosing?: boolean;
}

interface ToastContextType {
    showToast: (type: ToastType, title: string, message?: string, duration?: number) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export function ToastProvider({children}: { children: ReactNode }) {
    const [toasts, setToasts] = useState<ToastMessage[]>([]);

    const removeToast = useCallback((id: string) => {
        setToasts(current =>
            current.map(t => t.id === id ? {...t, isClosing: true} : t)
        );

        setTimeout(() => {
            setToasts(current => current.filter(t => t.id !== id));
        }, 300);
    }, []);

    const showToast = useCallback((type: ToastType, title: string, message?: string, duration = 4000) => {
        const id = uuidv4();
        setToasts(current => [...current, {id, type, title, message, duration}]);

        if (duration > 0) {
            setTimeout(() => removeToast(id), duration);
        }
    }, [removeToast]);

    const getIcon = (type: ToastType) => {
        switch (type) {
            case 'success':
                return <FaCheckCircle/>;
            case 'error':
                return <FaExclamationCircle/>;
            case 'warning':
                return <FaExclamationTriangle/>;
            case 'info':
                return <FaInfoCircle/>;
        }
    };

    return (
        <ToastContext.Provider value={{showToast}}>
            {children}

            <div className={styles.toastContainer}>
                {toasts.map(toast => (
                    <div
                        key={toast.id}
                        className={clsx(styles.toast, styles[toast.type], toast.isClosing && styles.closing)}
                    >
                        <div className={styles.icon}>{getIcon(toast.type)}</div>
                        <div className={styles.content}>
                            <p className={styles.title}>{toast.title}</p>
                            {toast.message && <p className={styles.message}>{toast.message}</p>}
                        </div>
                        <button className={styles.closeBtn} onClick={() => removeToast(toast.id)}>
                            <FaTimes/>
                        </button>
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    );
}

export const useToast = () => {
    const context = useContext(ToastContext);
    if (!context) throw new Error("useToast must be used within a ToastProvider");
    return context;
};