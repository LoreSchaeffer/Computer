package it.lycoris.lycoscript.idea.highlighting;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import it.lycoris.lycoscript.compiler.parser.LycoScriptLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.util.Set;

public class LycoScriptLexerAdapter extends LexerBase {
    private LycoScriptLexer antlrLexer;
    private Token currentToken;
    private CharSequence buffer;
    private int endOffset;

    private static final Set<String> KEYWORDS = Set.of(
            "struct",
            "void",
            "const",
            "while",
            "for",
            "if",
            "else",
            "return",
            "int",
            "byte",
            "boolean",
            "char",
            "string",
            "true",
            "false",
            "import",
            "native"
    );

    @Override
    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.endOffset = endOffset;

        antlrLexer = new LycoScriptLexer(CharStreams.fromString(buffer.toString()));

        do {
            advance();
        } while (currentToken != null && getTokenStart() < startOffset);
    }

    @Override
    public void advance() {
        currentToken = antlrLexer.nextToken();
        if (currentToken.getType() == Token.EOF || getTokenStart() >= endOffset) currentToken = null;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public IElementType getTokenType() {
        if (currentToken == null) return null;

        int type = currentToken.getType();

        if (type == LycoScriptLexer.STRING_LITERAL || type == LycoScriptLexer.CHAR_LITERAL) return LycoScriptTokenTypes.STRING;
        if (type == LycoScriptLexer.NUMBER || type == LycoScriptLexer.HEX_NUMBER) return LycoScriptTokenTypes.NUMBER;
        if (type == LycoScriptLexer.LINE_COMMENT || type == LycoScriptLexer.BLOCK_COMMENT) return LycoScriptTokenTypes.COMMENT;
        if (type == LycoScriptLexer.WS) return TokenType.WHITE_SPACE;
        if (type == LycoScriptLexer.ANY_OTHER) return LycoScriptTokenTypes.BAD_CHARACTER;

        String text = currentToken.getText();

        if (type == LycoScriptLexer.ID) {
            if (KEYWORDS.contains(text)) return LycoScriptTokenTypes.KEYWORD;
            return LycoScriptTokenTypes.IDENTIFIER;
        }

        if (KEYWORDS.contains(text)) return LycoScriptTokenTypes.KEYWORD;
        return LycoScriptTokenTypes.OPERATOR;
    }

    @Override
    public int getTokenStart() {
        return currentToken.getStartIndex();
    }

    @Override
    public int getTokenEnd() {
        return currentToken.getStopIndex() + 1;
    }

    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return endOffset;
    }
}