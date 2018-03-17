package aldor.module.template;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public final class TemplateFiles {
    private static final Logger LOG = Logger.getInstance(TemplateFiles.class);

    public static void saveFile(@NotNull VirtualFile file, @NotNull String templateName, @Nullable Map<String, String> templateAttributes)
            throws ConfigurationException {
        FileTemplateManager manager = FileTemplateManager.getDefaultInstance();
        FileTemplate template = manager.getInternalTemplate(templateName);
        try {
            appendToFile(file, (templateAttributes != null) ? template.getText(templateAttributes) : template.getText());
        }
        catch (IOException e) {
            LOG.warn(String.format("Unexpected exception on creating %s", templateName), e);
            //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
            throw new ConfigurationException(
                    e.getMessage(), String.format("Can't apply %s template config text", templateName));
        }
    }

    public static void appendToFile(@NotNull VirtualFile file, @NotNull String text) throws IOException {
        String lineSeparator = LoadTextUtil.detectLineSeparator(file, true);
        if (lineSeparator == null) {
            lineSeparator = CodeStyleSettingsManager.getSettings(ProjectManagerEx.getInstanceEx().getDefaultProject()).getLineSeparator();
        }
        final String existingText = StringUtil.trimTrailing(VfsUtilCore.loadText(file));
        @SuppressWarnings("StringConcatenationMissingWhitespace")
        String content = (StringUtil.isNotEmpty(existingText) ? existingText + lineSeparator : "") +
                StringUtil.convertLineSeparators(text, lineSeparator);
        VfsUtil.saveText(file, content);
    }

}
