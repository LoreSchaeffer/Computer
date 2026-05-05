package it.lycoris.lycoscript.idea.highlighting;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import it.lycoris.lycoscript.compiler.parser.LycoScriptLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

/**
 * Adapts the ANTLR4 Lexer from the compiler library to the IntelliJ Platform Lexer interface.
 * Implements strict index bound checking for end-of-file (EOF) scenarios.
 */
public class LycoScriptLexerAdapter extends LexerBase {

    private final LycoScriptLexer antlrLexer;
    private Token currentToken;
    private CharSequence buffer;
    private int startOffset;
    private int endOffset;

    public LycoScriptLexerAdapter() {
        this.antlrLexer = new LycoScriptLexer(CharStreams.fromString(""));
    }

    @Override
    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;

        String textToLex = buffer.subSequence(startOffset, endOffset).toString();
        this.antlrLexer.setInputStream(CharStreams.fromString(textToLex));

        this.advance();
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public IElementType getTokenType() {
        if (this.currentToken == null || this.currentToken.getType() == Token.EOF) {
            return null;
        }
        return LycoScriptTokenTypes.getElementType(this.currentToken.getType());
    }

    @Override
    public int getTokenStart() {
        // Fallback to startOffset if token is null to prevent out-of-bounds exceptions
        if (this.currentToken == null) {
            return this.startOffset;
        }
        return this.startOffset + this.currentToken.getStartIndex();
    }

    @Override
    public int getTokenEnd() {
        // Fallback to endOffset if token is null to prevent out-of-bounds exceptions
        if (this.currentToken == null) {
            return this.endOffset;
        }
        return this.startOffset + this.currentToken.getStopIndex() + 1;
    }

    @Override
    public void advance() {
        this.currentToken = this.antlrLexer.nextToken();
    }

    @Override
    public CharSequence getBufferSequence() {
        return this.buffer;
    }

    @Override
    public int getBufferEnd() {
        return this.endOffset;
    }
}