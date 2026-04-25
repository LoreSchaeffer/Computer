import '@xyflow/react/dist/style.css';
import './index.css'
import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import App from './App.tsx'
import {EditorProvider} from "./context/EditorContext.tsx";
import {ReactFlowProvider} from "@xyflow/react";
import {ModalProvider} from "./context/ModalContext.tsx";
import {ContextMenuProvider} from "./context/ContextMenuContext.tsx";

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <ModalProvider>
            <ContextMenuProvider>
                <EditorProvider>
                    <ReactFlowProvider>
                        <App/>
                    </ReactFlowProvider>
                </EditorProvider>
            </ContextMenuProvider>
        </ModalProvider>
    </StrictMode>,
)
