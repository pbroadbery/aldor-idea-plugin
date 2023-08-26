package aldor.module.template;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.module.AldorEnabledModuleExtension;
import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleExtension;
import aldor.build.module.AldorModuleType;
import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.module.AldorFacetProperties;
import aldor.builder.jps.module.MakeConvention;
import aldor.module.template.wizard.WizardCheckBox;
import aldor.module.template.wizard.WizardFieldContainer;
import aldor.module.template.wizard.WizardFieldDocumentation;
import aldor.module.template.wizard.WizardJdkSelector;
import aldor.sdk.aldor.AldorInstalledSdkType;
import aldor.util.TypedName;
import aldor.util.TypedTry;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AldorSimpleModuleBuilder extends AldorModuleBuilder {
    private static final TypedName<String> SELECTED_SDK_NAME_ID = TypedName.of(String.class, "SelectedSdk");
    private static final TypedName<Boolean> GENERATE_MAKEFILES_ID = TypedName.of(Boolean.class, "GenerateMakefiles");
    private static final Logger LOG = Logger.getInstance(AldorSimpleModuleBuilder.class);
    private final String aldorJdkName = null;
    private boolean createInitialStructure = true;
    private WizardFieldContainer fields = new WizardFieldContainer();
    private String relativeOutputDirectory = "out";

    public AldorSimpleModuleBuilder() {
        super(AldorModuleType.instance());
        createAdditionalFields();
    }

    public void setCreateInitialStructure(boolean flg) {
        this.createInitialStructure = flg;
    }

    @Override
    @Nullable
    @NonNls
    public String getBuilderId() {
        return "Simple-Aldor-Module";
    }

    @Override
    @NotNull
    protected List<WizardInputField<?>> getAdditionalFields() {
        return fields.fields();
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
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        LOG.info("Creating wizard steps");
        return ModuleWizardStep.EMPTY_ARRAY;
    }

    @Override
    public ModuleWizardStep[] createFinishingSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        LOG.info("Creating finishing steps");
        //return new ModuleWizardStep[] { new AldorNewModuleFacetStep(wizardContext.getProject(), this.properties)};
        return ModuleWizardStep.EMPTY_ARRAY;}

    @TestOnly
    public void setSdk(Sdk sdk) {
        this.fields.field(SELECTED_SDK_NAME_ID.name()).setValue(sdk.getName());
    }
    private String sdkName() {
        return fields.field(SELECTED_SDK_NAME_ID.name()).getValue();
    }


    @VisibleForTesting
    public void setRelativeBuildDirectory(String dir) {
        this.relativeOutputDirectory = dir;
    }

    @Override
    public void setupRootModel(@NotNull final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        LOG.info("Creating root model");
        super.setupRootModel(modifiableRootModel);

        LOG.info("Creating enabled extension");
        AldorEnabledModuleExtension extension = modifiableRootModel.getModuleExtension(AldorEnabledModuleExtension.class);
        extension.enabled(true);
        extension.commit();

        // FIXME: Use a form or wizard field
        LOG.info("Creating enabled extension");
        AldorModuleExtension aldorState = modifiableRootModel.getModuleExtension(AldorModuleExtension.class);
        aldorState.setState(aldorState.state().asBuilder().build());
        aldorState.commit();

        String contentEntryPath = getContentEntryPath();
        LOG.info("Adding content entries to "+ contentEntryPath);
        if (StringUtil.isEmpty(contentEntryPath)) {
            return;
        }

        ContentEntry entry = modifiableRootModel.getContentEntries()[0];
        VirtualFile contentRootDir = Objects.requireNonNull(entry.getFile());
        entry.addSourceFolder(contentRootDir.getUrl() + "/src", AldorSourceRootType.INSTANCE);

        if (createInitialStructure && this.fields.value(GENERATE_MAKEFILES_ID)) {
            createFileLayout(modifiableRootModel);
        }

    }

    private void createFileLayout(ModifiableRootModel rootModel) throws ConfigurationException {
        ContentEntry contentEntry = rootModel.getContentEntries()[0];
        SourceFolder sourceRoot = contentEntry.getSourceFolders()[0];
        AldorFacetProperties facetProperties = this.properties();
        String moduleName = rootModel.getModule().getName();
        String projectName = rootModel.getProject().getName();

        if (Objects.requireNonNull(contentEntry.getFile()).findChild("src") == null) {
            TypedTry.of(IOException.class, () -> contentEntry.getFile().createChildDirectory(this, "src"))
                    .orElseThrow(e -> new ConfigurationException(e.getMessage()));
        }
        if ((sourceRoot == null) || (sourceRoot.getFile() == null)) {
            throw new ConfigurationException("Missing source root " + sourceRoot);
        }
        try {
            VirtualFile file = getOrCreateExternalProjectConfigFile(sourceRoot.getFile(), "Makefile");
            if (file == null) {
                throw new ConfigurationException("Failed to find makefile");
            }

            Map<String, String> map = Maps.newHashMap();
            map.put("PROJECT", projectName);
            map.put("MODULE", moduleName);
            map.put("ALDOR_SDK", facetProperties.sdkName());
            TemplateFiles.saveFile(rootModel.getProject(), file, "Aldor Initial Makefile.none", map);

            file = getOrCreateExternalProjectConfigFile(sourceRoot.getFile(), "example.as");
            if (file == null) {
                throw new ConfigurationException("Unable to create src/example.as");
            }
            TemplateFiles.saveFile(rootModel.getProject(), file, "example.as", map);

            file = getOrCreateExternalProjectConfigFile(sourceRoot.getFile(), "test.as");
            if (file == null) {
                throw new ConfigurationException("Unable to create src/test.as");
            }
            TemplateFiles.saveFile(rootModel.getProject(), file, "test.as", map);

            VfsUtil.createDirectories(sourceRoot.getFile().getPath() + "/" + facetProperties.relativeOutputDirectory());
        } catch (IOException e) {
            LOG.error(e);
            throw new ConfigurationException("Failed to create output directory: " + e.getMessage());
        }
    }

    @Nullable
    private VirtualFile getOrCreateExternalProjectConfigFile(@NotNull VirtualFile parent, @NotNull String fileName) throws IOException {
        VirtualFile file = parent.findChild(fileName);
        if (file != null) {
            return file;
        }
        file = parent.createChildData(this, fileName);
        return file;
    }


    @Override
    protected void setupModule(Module module) throws ConfigurationException {
        LOG.debug("setup module.. module");
        super.setupModule(module);

        LOG.debug("setup module.. facet");
        AldorFacet.createFacetIfMissing(module, properties());
        LOG.debug("setup module.. done");
    }

    private void createAdditionalFields() {
        fields.add(new WizardJdkSelector(SELECTED_SDK_NAME_ID.name(), "Aldor Version", null,
                Collections.singleton(AldorInstalledSdkType.instance())));
        fields.add(new WizardFieldDocumentation(SELECTED_SDK_NAME_ID.name() + "DOC",
                "This is the location of your Aldor base directory." +
                        "  If your aldor executable is <path>/bin/aldor, then this should be <path>." +
                        "  Selecting '+ Add Aldor SDK' option in the drop down will let you add a new location."));
        fields.add(new LiveCheckboxValidator(GENERATE_MAKEFILES_ID.name(), "Create Makefiles", true));
        fields.add(new WizardFieldDocumentation(GENERATE_MAKEFILES_ID.name() + "DOC",
                "Should makefiles be created as part of setting up this module."));
    }

    public AldorFacetProperties properties() {
        return AldorFacetProperties.newBuilder().makeConvention(MakeConvention.Source).sdkName(sdkName()).relativeOutputDirectory(relativeOutputDirectory).build();
    }

    private static class LiveCheckboxValidator extends WizardCheckBox {
        private SettingsStep parent = null;

        LiveCheckboxValidator(String id, String label, boolean defaultValue) {
            super(id, label, defaultValue);
        }

        @Override
        public void addToSettings(SettingsStep settingsStep) {
            super.addToSettings(settingsStep);
            this.parent = settingsStep;
        }

        @Override
        public boolean validate() throws ConfigurationException {
            if ((parent == null) || (parent.getModuleNameLocationSettings() == null)) {
                return true;
            }
            String root = parent.getModuleNameLocationSettings().getModuleContentRoot();
            File file = new File(root, "src/Makefile");
            if (this.isSelected() && file.exists()) {
                throw new ConfigurationException("Cannot create " + file.getPath());
            }
            return true;
        }
    }

}
