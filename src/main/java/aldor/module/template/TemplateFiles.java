package aldor.module.template;

import com.intellij.application.options.CodeStyle;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public final class TemplateFiles {
    private static final Logger LOG = Logger.getInstance(TemplateFiles.class);

    public static void saveFile(Project project, @NotNull VirtualFile file, @NotNull String templateName, @Nullable Map<String, String> templateAttributes)
            throws ConfigurationException {
        FileTemplateManager manager = FileTemplateManager.getDefaultInstance();
        FileTemplate template = manager.getInternalTemplate(templateName);
        try {
            appendToFile(project, file, (templateAttributes != null) ? template.getText(templateAttributes) : template.getText());
        }
        catch (IOException e) {
            LOG.warn(String.format("Unexpected exception on creating %s", templateName), e);
            //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
            throw new ConfigurationException(
                    e.getMessage(), String.format("Can't apply %s template config text", templateName));
        }
    }

    public static void appendToFile(Project project, @NotNull VirtualFile file, @NotNull String text) throws IOException {
        String lineSeparator = LoadTextUtil.detectLineSeparator(file, true);
        if (lineSeparator == null) {
            CodeStyleSettings settings = CodeStyle.getSettings(project);
            lineSeparator = settings.getLineSeparator();
        }
        final String existingText = StringUtil.trimTrailing(VfsUtilCore.loadText(file));
        String content = (StringUtil.isNotEmpty(existingText) ? existingText + lineSeparator : "") +
                StringUtil.convertLineSeparators(text, lineSeparator);
        VfsUtil.saveText(file, content);
    }

}
