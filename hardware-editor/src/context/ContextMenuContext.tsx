import {createContext, type MouseEvent, type PropsWithChildren, useCallback, useContext, useRef, useState} from "react";
import type {ContextMenuItemData} from "../components/ui/context_menu/ContextMenuItem.tsx";
import type {Position} from "../types/common.ts";
import ContextMenu from "../components/ui/context_menu/ContextMenu.tsx";

type ShowContextMenuParams = {
    items: ContextMenuItemData[];
    event?: MouseEvent;
    customPos?: Position;
    onShow?: () => void;
    onHide?: () => void;
}

type ContextMenuContextType = {
    showContextMenu: (params: ShowContextMenuParams) => void;
    hideContextMenu: () => void;
}

type ContextMenuState = {
    isOpen: boolean;
    x: number;
    y: number;
    items: ContextMenuItemData[];
}

const ContextMenuContext = createContext<ContextMenuContextType | undefined>(undefined);

export const ContextMenuProvider = ({children}: PropsWithChildren) => {
    const [menuState, setMenuState] = useState<ContextMenuState>({
        isOpen: false,
        x: 0,
        y: 0,
        items: []
    });
    const onHideRef = useRef<(() => void) | undefined>(undefined);

    const showContextMenu = useCallback(({items, event, customPos, onShow, onHide}: ShowContextMenuParams) => {
        if (!event && !customPos) return;

        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }

        onHideRef.current = onHide;

        setMenuState({
            isOpen: true,
            x: customPos ? customPos.x : event?.pageX ?? 0,
            y: customPos ? customPos.y : event?.pageY ?? 0,
            items: items
        });

        onShow?.()
    }, []);

    const hideContextMenu = useCallback(() => {
        if (onHideRef.current) {
            onHideRef.current();
            onHideRef.current = undefined;
        }

        setMenuState((prev) => ({...prev, isOpen: false}));
    }, []);

    return (
        <ContextMenuContext.Provider value={{showContextMenu, hideContextMenu}}>
            {children}

            {menuState.isOpen && (
                <ContextMenu
                    isRoot={true}
                    pos={{x: menuState.x, y: menuState.y}}
                    items={menuState.items}
                    onClose={hideContextMenu}
                />
            )}
        </ContextMenuContext.Provider>
    );
}

export const useContextMenu = () => {
    const context = useContext(ContextMenuContext);
    if (context === undefined) throw new Error("useContextMenu must be used within a ContextMenuProvider");
    return context;
}