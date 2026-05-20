package it.lycoris.lycoscript.idea;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LycoHeaderFileType extends LanguageFileType {
    public static final LycoHeaderFileType INSTANCE = new LycoHeaderFileType();

    private LycoHeaderFileType() {
        super(LycoScriptLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "LycoHeader File";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "LycoScript header file";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "lh";
    }

    @Override
    public @Nullable Icon getIcon() {
        return LycoScriptIcons.FILE_LH;
    }
}
