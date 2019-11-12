package aldor.module.template;

import aldor.build.module.AldorMakeDirectoryOption;
import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModulePathService;
import aldor.build.module.AldorModuleType;
import aldor.sdk.aldor.AldorInstalledSdkType;
import com.google.common.collect.Maps;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SdkSettingsStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aldor.module.template.TemplateFiles.saveFile;

public class AldorSimpleModuleBuilder extends AldorModuleBuilder {
    private static final Logger LOG = Logger.getInstance(AldorSimpleModuleBuilder.class);
    private final Map<String, WizardInputField<?>> additionalFieldsByName = new HashMap<>();

    protected AldorSimpleModuleBuilder() {
        super(AldorModuleType.instance());
    }

    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
        return new SdkSettingsStep(settingsStep, this, id -> id instanceof AldorInstalledSdkType);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<WizardInputField> getAdditionalFields() {
        return Collections.emptyList();
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

        ContentEntry entry = modifiableRootModel.getContentEntries()[0];
        VirtualFile contentRootDir = entry.getFile();
        createFileLayout(contentRootDir, modifiableRootModel);

        if (entry.getFile() != null) {
            VirtualFile file = entry.getFile();

            if (file != null) {
                entry.addSourceFolder(file + "/src", false);
                entry.addExcludeFolder(file + "/src/out");
            }
        }
    }


    private void createFileLayout(VirtualFile contentRootDir, ModifiableRootModel model) throws ConfigurationException {
        VirtualFile file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath() + "/src", "Makefile");
        if (file == null) {
            return;
        }
        Map<String, String> map = Maps.newHashMap();
        map.put("PROJECT", model.getProject().getName());
        map.put("MODULE", model.getModule().getName());
        map.put("ALDOR_SDK", (model.getSdk() == null) ? "\"SDK path goes here\"" : model.getSdk().getHomePath());
        map.put("INITIAL_ALDOR_FILES", "example");
        saveFile(model.getProject(), file, "Makefile.none", map);

        file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath() + "/src", "example.as");
        if (file == null) {
            return;
        }
        saveFile(model.getProject(), file, "example.as", map);
        boolean created = FileUtilRt.createDirectory(new File(contentRootDir + "/src/out"));
        if (!created) {
            throw new ConfigurationException("Unable to create directory " + contentRootDir+"/src/out");
        }
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


    @Override
    protected void setupModule(Module module) throws ConfigurationException {
        super.setupModule(module);
        AldorModulePathService pathService = AldorModulePathService.getInstance(module);
        pathService.getState().setOutputDirectory("out/ao");
        pathService.getState().setMakeDirectory(AldorMakeDirectoryOption.Source);
    }
}
