package it.lycoris.lycoscript.idea.highlighting;

import com.intellij.psi.tree.IElementType;
import it.lycoris.lycoscript.idea.LycoScriptLanguage;

public class LycoScriptTokenTypes {
    public static final IElementType KEYWORD = new LycoScriptElementType("LYCO_KEYWORD");
    public static final IElementType STRING = new LycoScriptElementType("LYCO_STRING");
    public static final IElementType NUMBER = new LycoScriptElementType("LYCO_NUMBER");
    public static final IElementType IDENTIFIER = new LycoScriptElementType("LYCO_IDENTIFIER");
    public static final IElementType COMMENT = new LycoScriptElementType("LYCO_COMMENT");
    public static final IElementType OPERATOR = new LycoScriptElementType("LYCO_OPERATOR");
    public static final IElementType BAD_CHARACTER = new LycoScriptElementType("LYCO_BAD_CHARACTER");

    public static class LycoScriptElementType extends IElementType {
        public LycoScriptElementType(String debugName) {
            super(debugName, LycoScriptLanguage.INSTANCE);
        }
    }
}
