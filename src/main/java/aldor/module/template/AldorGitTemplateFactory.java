package aldor.module.template;

import aldor.build.module.AldorModuleType;
import aldor.ui.AldorIcons;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import com.intellij.platform.templates.BuilderBasedTemplate;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AldorGitTemplateFactory extends ProjectTemplatesFactory {
    private static final ProjectTemplate[] EMPTY_TEMPLATES = new ProjectTemplate[0];
    public static final int ALDOR_GROUP_WEIGHT = 1200;

    private static class TemplateRegistry {
        private final String name;
        private final List<ProjectTemplate> templates;

        TemplateRegistry(String name, List<ProjectTemplate> templates) {
            this.name = name;
            this.templates = new ArrayList<>(templates);
        }

        public String name() {
            return name;
        }

        public List<ProjectTemplate> templates() {
            return Collections.unmodifiableList(templates);
        }
    }

    private final List<TemplateRegistry> templateRegisties = Lists.newArrayList();

    AldorGitTemplateFactory() {
        templateRegisties.add(new TemplateRegistry("Aldor/Spad", Lists.newArrayList(
                new BuilderBasedTemplate(new AldorEmptyModuleBuilder())
        )));
        templateRegisties.add(new TemplateRegistry("Aldor", Lists.newArrayList(
                //new BuilderBasedTemplate(new AldorGitModuleBuilder("Aldor")),
                new BuilderBasedTemplate(new AldorSimpleModuleBuilder())
                )));


        templateRegisties.add(new TemplateRegistry("Spad", Lists.newArrayList(
                new BuilderBasedTemplate(new AldorGitModuleBuilder("Spad"))
                )));
    }

    @NotNull
    @Override
    public String[] getGroups() {
        List<String> groupNames = Lists.newArrayList();
        for (TemplateRegistry r: templateRegisties) {
            groupNames.add(r.name());
        }
        return groupNames.toArray(new String[templateRegisties.size()]);
    }

    @Override
    public int getGroupWeight(String group) {
        return ALDOR_GROUP_WEIGHT;
    }

    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context) {
        Optional<TemplateRegistry> registry = templateRegisties.stream().filter(r -> r.name().equals(group)).findFirst();
        return registry.map(r -> r.templates().toArray(new ProjectTemplate[r.templates().size()])).orElse(EMPTY_TEMPLATES);
    }

    @Override
    public Icon getGroupIcon(String group) {
        return AldorIcons.MODULE;
    }

    @Nullable
    @Override
    public String getParentGroup(String group) {
        return null;
    }

    private abstract static class AldorTemplateModuleBuilder extends AldorModuleType.AldorModuleBuilder {


        AldorTemplateModuleBuilder(AldorModuleType type) {
            super(type);
        }

        @Override
        public String getPresentableName() {
            return "Empty Aldor/Spad module";
        }

        @Override
        public String getDescription() {
            return "Empty Aldor/Spad module - do as you will..";
        }


        protected void createContentRoot(ModifiableRootModel modifiableRootModel) {
            String contentEntryPath = getContentEntryPath();
            if (StringUtil.isEmpty(contentEntryPath)) {
                return;
            }
            File contentRootDir = new File(contentEntryPath);
            FileUtilRt.createDirectory(contentRootDir);
            LocalFileSystem fileSystem = LocalFileSystem.getInstance();
            VirtualFile modelContentRootDir = fileSystem.refreshAndFindFileByIoFile(contentRootDir);
            if (modelContentRootDir == null) {
                return;
            }

            modifiableRootModel.addContentEntry(modelContentRootDir);
        }
    }

    private static class AldorEmptyModuleBuilder extends AldorModuleType.AldorModuleBuilder {
        AldorEmptyModuleBuilder() {
            super(AldorModuleType.instance());
        }
    }

    private final class AldorGitModuleBuilder extends AldorModuleType.AldorModuleBuilder {
        private final String name;

        private AldorGitModuleBuilder(String name) {
            super(AldorModuleType.instance());
            this.name = name;
        }

        @Override
        public String getPresentableName() {
            return name + " Git Module";
        }

        @Override
        public String getDescription() {
            return name + " Module cloned from git repository";
        }
    }

    private static class AldorSimpleModuleBuilder extends AldorModuleType.AldorModuleBuilder {
        private static final Logger LOG = Logger.getInstance(AldorSimpleModuleBuilder.class);

        protected AldorSimpleModuleBuilder() {
            super(AldorModuleType.instance());
        }

        @Override
        public String getPresentableName() {
            return "Simple Aldor module";
        }

        @Override
        public String getDescription() {
            return "Aldor module with Makefile created";
        }

        @Override
        public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
            super.setupRootModel(modifiableRootModel);
            String contentEntryPath = getContentEntryPath();
            if (StringUtil.isEmpty(contentEntryPath)) {
                return;
            }

            File contentRootDir = new File(contentEntryPath);
            createFileLayout(contentRootDir, modifiableRootModel);
        }

        private void createFileLayout(File contentRootDir, ModifiableRootModel model) throws ConfigurationException {
            VirtualFile file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath(), "Makefile");
            if (file == null) {
                return;
            }
            Map<String, String> map = Maps.newHashMap();
            map.put("PROJECT", model.getProject().getName());
            map.put("MODULE", model.getModule().getName());
            saveFile(file, "Makefile.none", map);
        }

        @Nullable
        private static VirtualFile getOrCreateExternalProjectConfigFile(@NotNull String parent, @NotNull String fileName) {
            File file = new File(parent, fileName);
            FileUtilRt.createIfNotExists(file);
            return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        }


        private VirtualFile getContentRoot() {
            if (getContentEntryPath() == null) {
                throw new IllegalStateException("Missing content root");
            }
            LocalFileSystem fileSystem = LocalFileSystem.getInstance();
            return fileSystem.findFileByIoFile(new File(getContentEntryPath()));
        }

        private static void saveFile(@NotNull VirtualFile file, @NotNull String templateName, @Nullable Map<String, String> templateAttributes)
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
}
