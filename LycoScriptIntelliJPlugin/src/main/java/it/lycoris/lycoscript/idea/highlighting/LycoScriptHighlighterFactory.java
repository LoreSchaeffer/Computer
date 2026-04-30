package it.lycoris.lycoscript.idea.highlighting;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory class required by the IntelliJ Platform to instantiate the syntax highlighter.
 */
public class LycoScriptHighlighterFactory extends SyntaxHighlighterFactory {

    @NotNull
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
        return new LycoScriptSyntaxHighlighter();
    }
}