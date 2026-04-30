package it.lycoris.lycoscript.idea;

import com.intellij.lang.Language;

/**
 * Defines the LycoScript language for the IntelliJ Platform.
 * Follows the Singleton pattern as required by the IDE architecture.
 */
public class LycoScriptLanguage extends Language {
    public static final LycoScriptLanguage INSTANCE = new LycoScriptLanguage();

    private LycoScriptLanguage() {
        super("LycoScript");
    }
}
