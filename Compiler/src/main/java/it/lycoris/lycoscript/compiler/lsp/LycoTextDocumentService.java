package it.lycoris.lycoscript.compiler.lsp;

import it.lycoris.lycoscript.compiler.parser.LycoScriptLexer;
import it.lycoris.lycoscript.compiler.parser.LycoScriptParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LycoTextDocumentService implements TextDocumentService {
    private LanguageClient client;
    private final Map<String, String> openDocuments = new ConcurrentHashMap<>();
    private final Map<String, List<CompletionItem>> dynamicCompletions = new ConcurrentHashMap<>();

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();
        openDocuments.put(uri, text);
        validateDocument(uri, text);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getContentChanges().getFirst().getText();
        openDocuments.put(uri, text);
        validateDocument(uri, text);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        openDocuments.remove(uri);
        dynamicCompletions.remove(uri);
        client.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<>()));
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {}

    private void validateDocument(String uri, String text) {
        CompletableFuture.runAsync(() -> {
            LycoErrorListener errorListener = new LycoErrorListener();

            LycoScriptLexer lexer = new LycoScriptLexer(CharStreams.fromString(text));
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            LycoScriptParser parser = new LycoScriptParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            ParseTree tree = parser.program();

            LycoCompletionListener completionListener = new LycoCompletionListener();
            ParseTreeWalker.DEFAULT.walk(completionListener, tree);
            dynamicCompletions.put(uri, completionListener.getCompletions());

            PublishDiagnosticsParams diagnosticsParams = new PublishDiagnosticsParams(uri, errorListener.getDiagnostics());
            client.publishDiagnostics(diagnosticsParams);
        });
    }

    @Override
    public CompletableFuture<org.eclipse.lsp4j.jsonrpc.messages.Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> items = new ArrayList<>();
            String uri = position.getTextDocument().getUri();

            List<CompletionItem> userItems = dynamicCompletions.get(uri);
            if (userItems != null) items.addAll(userItems);

            String[] keywords = {"int", "byte", "boolean", "string", "if", "else", "while", "for", "return", "struct", "native", "import"};
            for (String kw : keywords) {
                CompletionItem item = new CompletionItem(kw);
                item.setKind(CompletionItemKind.Keyword);
                items.add(item);
            }

            // TODO To be implemented lather

            CompletionItem printFunc = new CompletionItem("print");
            printFunc.setKind(CompletionItemKind.Function);
            printFunc.setInsertText("print(${1:\"text\"}, ${2:x}, ${3:y});");
            printFunc.setInsertTextFormat(InsertTextFormat.Snippet);
            printFunc.setDetail("void print(string text, byte x, byte y)");
            items.add(printFunc);

            CompletionItem clearFunc = new CompletionItem("clearScreen");
            clearFunc.setKind(CompletionItemKind.Function);
            clearFunc.setInsertText("clearScreen();");
            clearFunc.setInsertTextFormat(InsertTextFormat.Snippet);
            clearFunc.setDetail("void clearScreen()");
            items.add(clearFunc);

            return org.eclipse.lsp4j.jsonrpc.messages.Either.forLeft(items);
        });
    }
}