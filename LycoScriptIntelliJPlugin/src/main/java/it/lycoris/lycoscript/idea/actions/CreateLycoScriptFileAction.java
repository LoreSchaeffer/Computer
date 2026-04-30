package it.lycoris.lycoscript.idea.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import it.lycoris.lycoscript.idea.LycoScriptIcons;
import org.jetbrains.annotations.NotNull;

/**
 * Action triggered when the user selects "New -> LycoScript File".
 * Prompts the user for a file name and generates it using the predefined Velocity template.
 */
public class CreateLycoScriptFileAction extends CreateFileFromTemplateAction {

    /**
     * Initializes the action with its display name, description, and icon.
     */
    public CreateLycoScriptFileAction() {
        super("LycoScript File", "Creates a new LycoScript source file", LycoScriptIcons.FILE);
    }

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle("New LycoScript File")
                // The third parameter must match the exact name of the .ft file without the extension
                .addKind("Empty file", LycoScriptIcons.FILE, "LycoScript File");
    }

    @Override
    protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
        return "Create LycoScript File: " + newName;
    }
}