package it.lycoris.lycoscript.idea;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Registers the .ls file extension and maps it to the LycoScript language and icon.
 */
public class LycoScriptFileType extends LanguageFileType {
    public static final LycoScriptFileType INSTANCE = new LycoScriptFileType();

    private LycoScriptFileType() {
        super(LycoScriptLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "LycoScript File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "LycoScript source file for Lyco-8";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "ls";
    }

    @Override
    public Icon getIcon() {
        return LycoScriptIcons.FILE;
    }
}