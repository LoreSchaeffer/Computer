package it.lycoris.lycoscript.idea.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import it.lycoris.lycoscript.compiler.parser.LycoScriptLexer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Maps logical syntax elements to visual text attributes (colors, bold styles, etc.).
 */
public class LycoScriptSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("LYCOSCRIPT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("LYCOSCRIPT_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("LYCOSCRIPT_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("LYCOSCRIPT_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    private static final Map<IElementType, TextAttributesKey[]> COLOR_MAP = new HashMap<>();

    static {
        // Map ANTLR tokens to specific Text Attributes
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.CONST), KEYWORD_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.BYTE), KEYWORD_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.POINTER), KEYWORD_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.VOID), KEYWORD_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.IF), KEYWORD_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.ELSE), KEYWORD_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.WHILE), KEYWORD_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.RETURN), KEYWORD_KEYS);

        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.NUMBER), NUMBER_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.HEX_NUMBER), NUMBER_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.STRING_LITERAL), STRING_KEYS);

        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.LINE_COMMENT), COMMENT_KEYS);
        COLOR_MAP.put(LycoScriptTokenTypes.getElementType(LycoScriptLexer.BLOCK_COMMENT), COMMENT_KEYS);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new LycoScriptLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return COLOR_MAP.getOrDefault(tokenType, EMPTY_KEYS);
    }
}