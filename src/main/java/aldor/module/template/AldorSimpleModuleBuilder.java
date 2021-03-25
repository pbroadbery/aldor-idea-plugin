package aldor.module.template;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleType;
import aldor.builder.jps.AldorModuleExtensionProperties;
import aldor.builder.jps.AldorSourceRootType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static aldor.module.template.TemplateFiles.saveFile;

public class AldorSimpleModuleBuilder extends AldorModuleBuilder {
    private static final Logger LOG = Logger.getInstance(AldorSimpleModuleBuilder.class);
    private String aldorJdkName = null;
    private boolean createInitialStructure = true;
    private final AtomicReference<AldorModuleExtensionProperties> properties = new AtomicReference<>(new AldorModuleExtensionProperties());
    public AldorSimpleModuleBuilder() {
        super(AldorModuleType.instance());
    }

    public void setCreateInitialStructure(boolean flg) {
        this.createInitialStructure = flg;
    }

    @Override
    public @NotNull List<WizardInputField<?>> getAdditionalFields() {
        return Collections.emptyList();
    }

    @Override
    public String getPresentableName() {
        return "Simple Aldor module";
    }

    @Override
    public String getDescription() {
        return "Aldor module with Makefile created<b>" +
                "Please treat this as experimental - not all features work as yet";
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[] { new AldorNewModuleFacetStep(wizardContext.getProject(), this.properties)};
    }

    @VisibleForTesting
    public void setSdk(Sdk sdk) {
        this.properties.set(properties.get().asBuilder().setSdkName(sdk.getName()).build());
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
        if (this.createInitialStructure) {
            createFileLayout(contentRootDir, modifiableRootModel);

            if (entry.getFile() != null) {
                VirtualFile file = entry.getFile();

                if (file != null) {
                    entry.addSourceFolder(file + "/src", AldorSourceRootType.INSTANCE);
                    entry.addExcludeFolder(file + "/src/out");
                }
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
        saveFile(model.getProject(), file, "Aldor Initial Makefile.none", map);

        file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath() + "/src", "example.as");
        if (file == null) {
            return;
        }
        saveFile(model.getProject(), file, "Aldor Initial.as", map);
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
        LOG.debug("setup module.. facet");
        AldorFacet.createFacetIfMissing(module, properties.get());
        LOG.debug("setup module.. module");
        super.setupModule(module);
        LOG.debug("setup module.. done");
    }

    public AldorModuleExtensionProperties properties() {
        return properties.get();
    }

}
