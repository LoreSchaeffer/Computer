package it.lycoris.lycoscript.compiler.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.concurrent.CompletableFuture;

public class LycoLanguageServer implements LanguageServer, LanguageClientAware {
    private LanguageClient client;
    private final LycoTextDocumentService textDocumentService;
    private final LycoWorkspaceService workspaceService;

    public LycoLanguageServer() {
        this.textDocumentService = new LycoTextDocumentService();
        this.workspaceService = new LycoWorkspaceService();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();

        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

        CompletionOptions completionOptions = new CompletionOptions();
        completionOptions.setResolveProvider(false);
        capabilities.setCompletionProvider(completionOptions);

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
        this.textDocumentService.setClient(client);
    }
}