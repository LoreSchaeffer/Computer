import styles from './Input.module.css';
import {type CSSProperties, forwardRef, type InputHTMLAttributes, type ReactNode, useImperativeHandle, useRef} from "react";
import {clsx} from "clsx";
import {FaMinus, FaPlus} from "react-icons/fa";
import type {BackgroundVariant} from "../../../types/common.ts";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
    icon?: ReactNode;
    iconSettings?: {
        onClick?: () => void;
        hoverColor?: string;
        customStyles?: CSSProperties;
    };
    error?: string;
    spinner?: boolean;
    background?: BackgroundVariant;
}

const Input = forwardRef<HTMLInputElement, InputProps>(({
                                                            icon,
                                                            iconSettings,
                                                            error,
                                                            spinner = false,
                                                            background = 'base',
                                                            type = 'text',
                                                            step = 1,
                                                            min,
                                                            max,
                                                            className,
                                                            ...props
                                                        }, ref) => {
    const internalRef = useRef<HTMLInputElement>(null);
    useImperativeHandle(ref, () => internalRef.current as HTMLInputElement);

    const dispatchChangeEvent = (newValue: string) => {
        const input = internalRef.current;
        if (!input) return;
        const nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")?.set;
        nativeInputValueSetter?.call(input, newValue);
        input.dispatchEvent(new Event('change', {bubbles: true}));
    };

    const handleIncrement = () => {
        if (!internalRef.current) return;
        const newValue = (parseFloat(internalRef.current.value) || 0) + (parseFloat(step.toString()) || 1);
        if (max !== undefined && newValue > parseFloat(max.toString())) return;
        dispatchChangeEvent(String(newValue));
    };

    const handleDecrement = () => {
        if (!internalRef.current) return;
        const newValue = (parseFloat(internalRef.current.value) || 0) - (parseFloat(step.toString()) || 1);
        if (min !== undefined && newValue < parseFloat(min.toString())) return;
        dispatchChangeEvent(String(newValue));
    };

    const inputType = spinner ? 'number' : type;

    return (
        <div className={clsx(styles.formInput, className)}>
            <div className={clsx(
                styles.inputWrapper,
                styles[background],
                error && styles.error,
                spinner && styles.spinnerMode
            )}>
                {spinner && (
                    <button type="button" className={styles.spinnerBtn} onClick={handleDecrement} tabIndex={-1}>
                        <FaMinus/>
                    </button>
                )}

                <input
                    ref={internalRef}
                    type={inputType}
                    step={step}
                    min={min}
                    max={max}
                    className={clsx(styles.input, spinner ? styles.spinnerInput : styles.standardInput)}
                    {...props}
                />

                {spinner && (
                    <button type="button" className={styles.spinnerBtn} onClick={handleIncrement} tabIndex={-1}>
                        <FaPlus/>
                    </button>
                )}

                {!spinner && icon && (
                    <span
                        className={clsx(styles.icon, iconSettings?.onClick && styles.clickable)}
                        onClick={iconSettings?.onClick}
                        style={iconSettings?.customStyles}
                    >
                        {icon}
                    </span>
                )}
            </div>
            {error && <div className={styles.errorMessage}>{error}</div>}
        </div>
    );
});

export default Input;