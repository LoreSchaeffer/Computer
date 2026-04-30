import styles from './Button.module.css';
import type {ButtonHTMLAttributes, ReactNode} from 'react';
import clsx from 'clsx';
import type {Variant} from "../../../types/common.ts";

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: Variant;
    icon?: ReactNode;
    children?: ReactNode;
}

export function Button({
                           variant = 'primary',
                           className,
                           icon,
                           children,
                           disabled,
                           ...props
                       }: ButtonProps) {
    return (
        <button
            className={clsx(
                styles.button,
                styles[variant],
                className
            )}
            disabled={disabled}
            {...props}
        >
            {icon}
            {children}
        </button>
    );
}