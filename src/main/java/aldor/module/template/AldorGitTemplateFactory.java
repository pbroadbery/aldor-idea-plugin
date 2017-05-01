package aldor.module.template;

import aldor.build.module.AldorModuleType;
import aldor.sdk.AldorLocalSdkType;
import aldor.ui.AldorIcons;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ContentEntry;
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
import java.util.stream.Collectors;

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
                new BuilderBasedTemplate(new AldorGitModuleBuilder("Aldor")),
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

    private static class AldorEmptyModuleBuilder extends AldorModuleType.AldorModuleBuilder {
        AldorEmptyModuleBuilder() {
            super(AldorModuleType.instance());
        }
    }

    private static final class AldorGitModuleBuilder extends AldorModuleType.AldorModuleBuilder {
        private static final String FLD_SOURCEDIR = "aldor";
        private static final String FLD_USEEXISTING = "useExisting";
        private static final String FLD_BUILDDIR = "build";
        private final String name;
        private final Map<String, WizardInputField<?>> additionalFieldsByName;
        private final List<WizardInputField<?>> additionalFields;

        private AldorGitModuleBuilder(String name) {
            super(AldorModuleType.instance());
            this.name = name;
            additionalFields = createAdditionalFields();
            this.additionalFieldsByName = additionalFields.stream().collect(Collectors.toMap(WizardInputField::getId, f -> f));
        }

        private List<WizardInputField<?>> createAdditionalFields() {
            List<WizardInputField<?>> fields = new ArrayList<>();
            fields.add(new WizardCheckBox(FLD_USEEXISTING, "Use existing configuration", true));
            fields.add(new WizardTextField(FLD_SOURCEDIR, "Source directory", "aldor", this::validateSourceDirectory));
            fields.add(new WizardTextField(FLD_BUILDDIR, "Build directory", "build", this::validateBuildDirectory));

            return fields;
        }

        @Nullable
        private String validateBuildDirectory(String path) {
            return validateDirectory(path) ? null : "Invalid build directory";
        }

        @Nullable
        private String validateSourceDirectory(String path) {
            return validateDirectory(path + "/.git") ? null: "Invalid source directory";
        }

        private boolean validateDirectory(String filePath) {
            if (StringUtil.isEmpty(filePath)) {
                return false;
            }

            return true;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public List<WizardInputField> getAdditionalFields() {
            return new ArrayList<>(additionalFieldsByName.values());
        }

        @Override
        public String getPresentableName() {
            return name + " Git Module";
        }

        @Override
        public String getDescription() {
            return name + " Module cloned from git repository";
        }

        @Override
        public boolean isSuitableSdkType(SdkTypeId sdkType) {
            return (sdkType instanceof AldorLocalSdkType);
        }

        @Override
        public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
            super.setupRootModel(modifiableRootModel);
            String contentEntryPath = getContentEntryPath();
            String sourceDirectory = additionalFieldsByName.get(FLD_SOURCEDIR).getValue();

            if (StringUtil.isEmpty(contentEntryPath)) {
                return;
            }

            ContentEntry entry = modifiableRootModel.getContentEntries()[0];
            if (entry.getFile() == null) {
                return;
            }

            VirtualFile gitDirectory = entry.getFile().findFileByRelativePath(sourceDirectory + "/.git");
            if (gitDirectory == null) {
                File file = new File(entry.getFile().getPath() + "/" + sourceDirectory);
                throw new ConfigurationException("Missing git repository - expecting repository in " + file);
            }

            String[] paths = { "aldor/lib/aldor", "aldor/lib/algebra" };
            for (String path: paths) {
                VirtualFile file = entry.getFile().findFileByRelativePath(sourceDirectory + "/" + path);
                if (file != null) {
                    entry.addSourceFolder(file, false);
                }
            }
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

            ContentEntry entry = modifiableRootModel.getContentEntries()[0];
            if (entry.getFile() != null) {
                VirtualFile file = entry.getFile().findFileByRelativePath(contentRootDir.getAbsolutePath());

                if (file != null) {
                    entry.addSourceFolder(file, false);
                }
            }

        }

        private void createFileLayout(File contentRootDir, ModifiableRootModel model) throws ConfigurationException {
            VirtualFile file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath(), "Makefile");
            if (file == null) {
                return;
            }
            Map<String, String> map = Maps.newHashMap();
            map.put("PROJECT", model.getProject().getName());
            map.put("MODULE", model.getModule().getName());
            map.put("ALDOR_SDK", (model.getSdk() == null) ? "\"SDK path goes here\"" : model.getSdk().getHomePath());
            map.put("INITIAL_ALDOR_FILES", "example");
            saveFile(file, "Makefile.none", map);

            file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath(), "example.as");
            if (file == null) {
                return;
            }
            saveFile(file, "example.as", map);

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
