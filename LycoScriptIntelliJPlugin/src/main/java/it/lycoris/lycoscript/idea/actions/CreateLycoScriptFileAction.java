package it.lycoris.lycoscript.idea.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiDirectory;
import it.lycoris.lycoscript.idea.LycoScriptIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CreateLycoScriptFileAction extends CreateFileFromTemplateAction {

    public CreateLycoScriptFileAction() {
        super("LycoScript File", "Creates a new LycoScript source or header file", LycoScriptIcons.FILE_LS);
    }

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory psiDirectory, CreateFileFromTemplateDialog.@NotNull Builder builder) {
        builder.setTitle("New LycoScript File")
                .addKind("Source File (.ls)", LycoScriptIcons.FILE_LS, "LycoScript")
                .addKind("Header File (.lh)", LycoScriptIcons.FILE_LH, "LycoHeader");
    }

    @Override
    protected @NlsContexts.Command String getActionName(PsiDirectory directory, @NonNls @NotNull String newName, @NonNls String templateName) {
        return "Create LycoScript File:" + newName;
    }
}
