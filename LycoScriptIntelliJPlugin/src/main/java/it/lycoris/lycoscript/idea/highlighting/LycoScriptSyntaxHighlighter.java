package it.lycoris.lycoscript.idea.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class LycoScriptSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey KEYWORD = createTextAttributesKey("LYCO_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING = createTextAttributesKey("LYCO_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER = createTextAttributesKey("LYCO_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey COMMENT = createTextAttributesKey("LYCO_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey IDENTIFIER = createTextAttributesKey("LYCO_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey OPERATOR = createTextAttributesKey("LYCO_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("LYCO_BAD_CHARACTER", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new LycoScriptLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(LycoScriptTokenTypes.KEYWORD)) return new TextAttributesKey[]{KEYWORD};
        if (tokenType.equals(LycoScriptTokenTypes.STRING)) return new TextAttributesKey[]{STRING};
        if (tokenType.equals(LycoScriptTokenTypes.NUMBER)) return new TextAttributesKey[]{NUMBER};
        if (tokenType.equals(LycoScriptTokenTypes.COMMENT)) return new TextAttributesKey[]{COMMENT};
        if (tokenType.equals(LycoScriptTokenTypes.IDENTIFIER)) return new TextAttributesKey[]{IDENTIFIER};
        if (tokenType.equals(LycoScriptTokenTypes.OPERATOR)) return new TextAttributesKey[]{OPERATOR};
        if (tokenType.equals(LycoScriptTokenTypes.BAD_CHARACTER)) return new TextAttributesKey[]{BAD_CHARACTER};
        return new TextAttributesKey[0];
    }
}