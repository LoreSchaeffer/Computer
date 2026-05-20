package it.lycoris.lycoscript.compiler.lsp;

import it.lycoris.lycoscript.compiler.parser.LycoScriptBaseListener;
import it.lycoris.lycoscript.compiler.parser.LycoScriptParser;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LycoCompletionListener extends LycoScriptBaseListener {
    private final Map<String, CompletionItem> completions = new HashMap<>();

    @Override
    public void enterGlobalVarDecl(LycoScriptParser.GlobalVarDeclContext ctx) {
        String name = ctx.identifier().getText();
        String type = ctx.type().getText();

        CompletionItem item = new CompletionItem(name);
        item.setKind(CompletionItemKind.Variable);
        item.setDetail("global " + type + " " + name);
        completions.put(name, item);
    }

    @Override
    public void enterLocalVarDecl(LycoScriptParser.LocalVarDeclContext ctx) {
        String name = ctx.identifier().getText();
        String type = ctx.type().getText();

        CompletionItem item = new CompletionItem(name);
        item.setKind(CompletionItemKind.Variable);
        item.setDetail("local " + type + " " + name);
        completions.put(name, item);
    }

    @Override
    public void enterFuncDef(LycoScriptParser.FuncDefContext ctx) {
        String name = ctx.identifier().getText();

        CompletionItem item = new CompletionItem(name);
        item.setKind(CompletionItemKind.Function);
        item.setDetail("function " + name);
        item.setInsertText(name + "();");
        completions.put(name, item);
    }

    @Override
    public void enterStructDef(LycoScriptParser.StructDefContext ctx) {
        String name = ctx.identifier().getText();

        CompletionItem item = new CompletionItem(name);
        item.setKind(CompletionItemKind.Struct);
        item.setDetail("struct " + name);
        completions.put(name, item);
    }

    public List<CompletionItem> getCompletions() {
        return new ArrayList<>(completions.values());
    }
}