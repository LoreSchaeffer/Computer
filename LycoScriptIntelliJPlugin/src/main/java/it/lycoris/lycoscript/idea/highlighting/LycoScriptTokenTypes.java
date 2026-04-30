package it.lycoris.lycoscript.idea.highlighting;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import it.lycoris.lycoscript.compiler.parser.LycoScriptLexer;
import it.lycoris.lycoscript.idea.LycoScriptLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps ANTLR token IDs to IntelliJ IElementType instances.
 */
public class LycoScriptTokenTypes {
    private static final Map<Integer, IElementType> TOKEN_CACHE = new HashMap<>();

    // Map to standard IntelliJ Platform primitive types
    public static final IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;
    public static final IElementType WHITE_SPACE = TokenType.WHITE_SPACE;

    /**
     * Retrieves or creates an IElementType for a given ANTLR token type.
     *
     * @param antlrTokenType The integer type ID provided by ANTLR.
     * @return The corresponding IElementType.
     */
    public static IElementType getElementType(int antlrTokenType) {
        if (antlrTokenType == LycoScriptLexer.EOF) {
            return null;
        }

        // Extremely important: Map ANTLR whitespace directly to IntelliJ native whitespace
        // This prevents highlighting breaks and exceptions in the IDE editor
        if (antlrTokenType == LycoScriptLexer.WS) {
            return WHITE_SPACE;
        }

        return TOKEN_CACHE.computeIfAbsent(antlrTokenType, (Integer typeId) -> {
            String name = LycoScriptLexer.VOCABULARY.getSymbolicName(typeId);
            if (name == null) {
                name = "UNKNOWN_TOKEN_" + typeId;
            }
            return new LycoScriptElementType(name);
        });
    }

    /**
     * Internal implementation of IElementType specifically for LycoScript.
     */
    private static class LycoScriptElementType extends IElementType {
        public LycoScriptElementType(@NotNull @NonNls String debugName) {
            super(debugName, LycoScriptLanguage.INSTANCE);
        }
    }
}