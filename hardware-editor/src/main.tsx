import '@xyflow/react/dist/style.css';
import './index.css'
import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import App from './App.tsx'
import {ReactFlowProvider} from "@xyflow/react";
import {ModalProvider} from "./context/ModalContext.tsx";
import {ContextMenuProvider} from "./context/ContextMenuContext.tsx";
import {WorkspaceProvider} from "./context/WorkspaceContext.tsx";
import {CanvasProvider} from "./context/CanvasContext.tsx";
import {ToastProvider} from "./context/ToastContext.tsx";

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <WorkspaceProvider>
            <CanvasProvider>
                <ToastProvider>
                    <ModalProvider>
                        <ContextMenuProvider>
                            <ReactFlowProvider>
                                <App/>
                            </ReactFlowProvider>
                        </ContextMenuProvider>
                    </ModalProvider>
                </ToastProvider>
            </CanvasProvider>
        </WorkspaceProvider>
    </StrictMode>,
)
