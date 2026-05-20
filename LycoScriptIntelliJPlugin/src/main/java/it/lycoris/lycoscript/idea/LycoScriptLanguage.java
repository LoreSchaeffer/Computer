package it.lycoris.lycoscript.idea;

import com.intellij.lang.Language;

public class LycoScriptLanguage extends Language {
    public static final LycoScriptLanguage INSTANCE = new LycoScriptLanguage();

    private LycoScriptLanguage() {
        super("LycoScript");
    }
}
