package aldor.module.template.git;

import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleType;
import aldor.builder.jps.AldorSourceRootType;
import aldor.module.template.detect.AldorRepoProjectDetector;
import aldor.module.template.detect.DetectedRootFacetSettings;
import aldor.module.template.wizard.WizardCheckBox;
import aldor.module.template.wizard.WizardFieldContainer;
import aldor.module.template.wizard.WizardTextField;
import aldor.sdk.aldor.AldorLocalSdkType;
import aldor.sdk.fricas.FricasLocalSdkType;
import com.intellij.ide.util.importProject.RootDetectionProcessor;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.ide.util.projectWizard.importSources.impl.ProjectFromSourcesBuilderImpl;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class AldorGitModuleBuilder extends AldorModuleBuilder {
    public static final TypedWizardField<WizardCheckBox> FLD_USEEXISTING = new TypedWizardField<>(WizardCheckBox.class, "useExisting");
    public static final TypedWizardField<WizardTextField> FLD_SOURCEDIR = new TypedWizardField<>(WizardTextField.class, "aldor");
    public static final TypedWizardField<WizardTextField> FLD_BUILDDIR = new TypedWizardField<>(WizardTextField.class, "build");
    public static final TypedWizardField<WizardTextField> FLD_CLONELOCATION = new TypedWizardField<>(WizardTextField.class, "cloneLocation");
    private final WizardFieldContainer fields = new WizardFieldContainer();
    private final GitModuleDetail detail;
    private static final Logger LOG = Logger.getInstance(AldorGitModuleBuilder.class);

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public AldorGitModuleBuilder(GitModuleType type) {
        super(AldorModuleType.instance());
        this.detail = type.fn().apply(this);
        createAdditionalFields();
    }

    @Override
    @NonNls
    public String getBuilderId() {
        return "Git-Aldor-Module-" + detail.name();
    }


    private void createAdditionalFields() {
        fields.add(new WizardCheckBox(FLD_USEEXISTING.name(), "Use existing configuration", true));
        fields.add(new WizardTextField(FLD_SOURCEDIR.name(), "Source directory", "aldor", this::validateSourceDirectory));
        fields.add(new WizardTextField(FLD_BUILDDIR.name(), "Build directory", "build", this::validateBuildDirectory));
        fields.add(new WizardTextField(FLD_CLONELOCATION.name(), "clone", "/home/pab/Work/aldorgit/utypes/aldor", this::validateCloneLocation));

        fields.field(FLD_USEEXISTING).getComponent().addItemListener(this::itemStateChanged);
    }

    @Nullable
    private String validateCloneLocation(String s) {
        if (s.startsWith("/")) {
            File file = new File(s);
            if (!file.isDirectory()) {
                return "Must be a directory: " + file;
            }
            File gitLocation = new File(file, ".git");
            if (!gitLocation.isDirectory()) {
                return "No git " + gitLocation;
            }
            return null;
        }
        else {
            return null;
        }
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
    @NotNull
    public List<WizardInputField<?>> getAdditionalFields() {
        return fields.fields();
    }

    @Override
    public String getPresentableName() {
        return detail.name() + " Git Module";
    }

    @Override
    public String getName() {
        return detail.name();
    }

    @Override
    public String getDescription() {
        return detail.name() + " Source Module cloned from git repository<br>\n" +
                " - Use to work with the "+ detail.name() + " sources<br>\n" +
                "<b>Not</b> fully implemented - may not work correctly";
    }


    @Override
    public void setupRootModel(@NotNull final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        super.setupRootModel(modifiableRootModel);
        detail.setupRootModel(modifiableRootModel);
    }

    Consumer<ModifiableModuleModel> commitThing = null;

    @Override
    public @NotNull Module createModule(@NotNull ModifiableModuleModel moduleModel) throws InvalidDataException, ConfigurationException {
        final ModuleType moduleType = getModuleType();
        final Module module = moduleModel.newModule(this.getModuleFilePath(), moduleType.getId());
        setupModule(module);
        if (commitThing != null) {
            commitThing.accept(moduleModel);
        }
        return module;
    }

    void setCommitThing(Consumer<ModifiableModuleModel> commitThing) {
        this.commitThing = commitThing;
    }
    @Override
    public @NotNull Module createAndCommitIfNeeded(@NotNull Project project, @Nullable ModifiableModuleModel model,
                                                   boolean runFromProjectWizard)
            throws InvalidDataException, ConfigurationException, IOException, JDOMException, ModuleWithNameAlreadyExists {
        return super.createAndCommitIfNeeded(project, model, runFromProjectWizard);
    }

    private void itemStateChanged(ItemEvent l) {
        if (l.getStateChange() == ItemEvent.SELECTED) {
            fields.field(FLD_SOURCEDIR).getComponent().setEnabled(true);
            fields.field(FLD_BUILDDIR).getComponent().setEnabled(true);
        } else {
            fields.field(FLD_SOURCEDIR).getComponent().setEnabled(false);
            fields.field(FLD_BUILDDIR).getComponent().setEnabled(false);
        }
    }

    @Override
    public ModuleWizardStep[] createFinishingSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        LOG.info("Creating finishing steps..." + fields);
        return new ModuleWizardStep[] {
                new CloneRepositoryStep(wizardContext, modulesProvider, fields, detail),
                new GitProjectFinaliseStep(wizardContext, modulesProvider)};
    }

    public GitModuleDetail createAldorModuleDetail() {
        return new AldorGitModuleDetail();
    }

    static enum CloneResult { OK, FAIL }

    public class AldorGitModuleDetail implements GitModuleDetail {

        public AldorGitModuleDetail() {
        }

        @Override
        public boolean isSuitableSdkType(SdkTypeId sdkType) {
            return (sdkType instanceof AldorLocalSdkType);
        }

        @Override
        public String name() {
            return "Aldor";
        }

        @Override
        public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        }

        boolean validate() {
            String file = fields.field(FLD_SOURCEDIR).getValue();
            VirtualFile root = VirtualFileManager.getInstance().findFileByNioPath(Path.of(file));
            return root != null;
        }

        @Override
        public void doClone(Project project) throws ConfigurationException {
            clone(project);
        }

        private void clone(Project project) throws ConfigurationException {
            LOG.info("Building clone settings");
            String file = fields.field(FLD_SOURCEDIR).getValue();
            String url = fields.field(FLD_CLONELOCATION).getValue();
            File entryPath = Optional.ofNullable(getContentEntryPath()).map(File::new)
                    .orElseThrow(() -> new ConfigurationException("Missing root"));
            File finalPath = new File(entryPath, file);
            if (!finalPath.exists()) {
                if (!finalPath.mkdirs()) {
                    throw new ConfigurationException("Failed to create: " + finalPath);
                }
            }
            if (!finalPath.isDirectory()) {
                throw new ConfigurationException("Not a directory: " + finalPath);
            }
            String actual = finalPath.getAbsolutePath();
            LOG.info("Clone starting: url: " + url + " to: " + actual);
            if (!GitCloneHelper.clone(project, url, actual, "aldor")) {
                throw new ConfigurationException("Failed to clone " + url + " to " + actual);
            }
            LOG.info("Cloned!");
        }
    }
    public class FricasGitModuleDetail implements GitModuleDetail {

        @Override
        public boolean isSuitableSdkType(SdkTypeId sdkType) {
            return (sdkType instanceof FricasLocalSdkType);
        }

        @Override
        public String name() {
            return "Fricas";
        }

        @Override
        public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
            String contentEntryPath = getContentEntryPath();
            String sourceDirectory = fields.field(FLD_SOURCEDIR).getValue();

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

            String[] paths = { "fricas/src/algebra" };
            for (String path: paths) {
                VirtualFile file = entry.getFile().findFileByRelativePath(sourceDirectory + "/" + path);
                if (file != null) {
                    entry.addSourceFolder(file, AldorSourceRootType.INSTANCE);
                }
            }
        }

        @Override
        public void doClone(Project project) {
        }
    }

    private class GitProjectFinaliseStep extends ModuleWizardStep {
        private static final Logger LOG = Logger.getInstance(GitProjectFinaliseStep.class);
        private final AldorRepoProjectDetector detector;
        private final WizardContext wizardContext;
        private final ProjectFromSourcesBuilderImpl fromSourcesBuilder;
        private final ModulesProvider modulesProvider;

        GitProjectFinaliseStep(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
            detector = ProjectStructureDetector.EP_NAME.findExtension(AldorRepoProjectDetector.class);
            this.wizardContext = wizardContext;
            this.modulesProvider = modulesProvider;
            fromSourcesBuilder = new ProjectFromSourcesBuilderImpl(wizardContext, modulesProvider);
        }

        private final JLabel label = new JLabel("Creating additional modules");
        @Override
        public JComponent getComponent() {
            return label;
        }

        @Override
        public void updateDataModel() {
            LOG.info("Updating datamodel...");
            String path = getContentEntryPath();
            RootDetectionProcessor processor = new RootDetectionProcessor(new File(path),
                    new ProjectStructureDetector[]{detector});
            Map<ProjectStructureDetector, List<DetectedProjectRoot>> detected = processor.runDetectors();
            List<DetectedProjectRoot> projectRoots = Optional.ofNullable(detected.get(detector)).orElse(Collections.emptyList());
            /*
            detector.setupProjectStructure(projectRoots, descriptor, (file, name) -> name);
            DetectedRootFacetSettings settings = new DetectedRootFacetSettings();
            detector.configure(descriptor, projectRoots, settings);
            */
            LOG.info("Found " + projectRoots.size() + " roots");
            detector.setupProjectStructure(projectRoots,
                    fromSourcesBuilder.getProjectDescriptor(detector),
                    fromSourcesBuilder);
            DetectedRootFacetSettings settings = new DetectedRootFacetSettings();
            detector.configure(fromSourcesBuilder.getProjectDescriptor(detector), projectRoots, settings);
            LOG.info("Updating datamodel complete");
            setCommitThing(rootModel -> {
                fromSourcesBuilder.commit(rootModel.getProject(), rootModel, modulesProvider);
            });
        }

        @Override
        public void onWizardFinished() throws CommitStepException {
            LOG.info("FINISHED!");
            //fromSourcesBuilder.commit(wizardContext.getProject());
            LOG.info("Committed changes");
        }
    }
}
