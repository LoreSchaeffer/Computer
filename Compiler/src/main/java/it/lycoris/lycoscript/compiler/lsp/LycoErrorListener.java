package it.lycoris.lycoscript.compiler.lsp;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

public class LycoErrorListener extends BaseErrorListener {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        // ANTLR lines are 1-based, LSP positions are 0-based
        int lspLine = line - 1;

        // Create a range covering the character where the error occurred
        Position start = new Position(lspLine, charPositionInLine);
        Position end = new Position(lspLine, charPositionInLine + 1); // Ideally, calculate the length of the offending token
        Range range = new Range(start, end);

        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setSeverity(DiagnosticSeverity.Error);
        diagnostic.setRange(range);
        diagnostic.setMessage(msg);
        diagnostic.setSource("LycoScript Compiler");

        diagnostics.add(diagnostic);
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
