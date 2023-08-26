package aldor.module.template.detect;

import aldor.builder.jps.module.ConfigRootFacetProperties;
import aldor.util.Streams;
import com.intellij.ide.util.importProject.ModuleDescriptor;
import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

class AldorConfigurationWizard extends ModuleWizardStep {
    private static final Logger LOG = Logger.getInstance(AldorConfigurationWizard.class);
    private final ProjectDescriptor projectDescriptor;
    private final ProjectFromSourcesBuilder builder;
    private final AldorRepoProjectDetector detector;
    private ConfigurationWizardConfigurable configurable = null;

    public AldorConfigurationWizard(AldorRepoProjectDetector detector,
                                    ProjectFromSourcesBuilder builder,
                                    ProjectDescriptor projectDescriptor,
                                    Icon icon) {
        this.detector = detector;
        this.projectDescriptor = projectDescriptor;
        this.builder = builder;
    }

    private DetectedRootFacetSettings calculateDefaultSettings() {
        Collection<DetectedProjectRoot> roots = builder.getProjectRoots(detector);
        DetectedRootFacetSettings settings = new DetectedRootFacetSettings();
        for (ConfigRootCandidate root: roots.stream().flatMap(Streams.filterAndCast(ConfigRootCandidate.class)).toList()) {
            var dir = root.getDirectory();
            ConfigRootFacetProperties properties = ConfigRootFacetProperties.newBuilder()
                    .setBuildDirectory(new File(dir.getParentFile().getParentFile(), "build").toString())
                    .setInstallDirectory(new File(dir.getParentFile().getParentFile(), "opt").toString())
                    .setDefined(true)
                    .build();
            settings.put(dir, properties);
        }
        return settings;
    }

    @Override
    public JComponent getComponent() {
        LOG.info("getComponent");
        if (configurable == null) {
            configurable = new ConfigurationWizardConfigurable();
        }
        configurable.initialise(calculateDefaultSettings());
        return configurable.getComponent();
    }

    /** Update UI from ModuleBuilder and WizardContext */
    @Override
    public void updateStep() {
        LOG.info("updateStep");

        DetectedRootFacetSettings defaultSettings = calculateDefaultSettings();
        LOG.info("updateStep::Roots: " + builder.getProjectRoots(detector) + " empty?: " + defaultSettings.isEmpty());
        configurable.initialise(defaultSettings);
    }

    @Override
    public void onStepLeaving() {
        LOG.info("onStepLeaving");
        super.onStepLeaving();
    }

    @Override
    public void disposeUIResources() {
        LOG.info("disposeUIResources");
        if (this.configurable != null) {
            configurable.disposeUIResources();
        }
    }

    @Override
    public void onWizardFinished() throws CommitStepException {
        LOG.info("onWizardFinished");

        for (ModuleDescriptor mod: this.projectDescriptor.getModules()) {
            LOG.info("onWizardFinished::Adding module " + mod.getName() + " " + mod.getContentRoots().stream().map(x -> x.toPath()).toList());
        }

        for (ConfigRootCandidate mod: this.builder.getProjectRoots(detector).stream().flatMap(Streams.filterAndCast(ConfigRootCandidate.class)).toList()) {
            String buildDirectory = configurable.currentState().get(mod.getDirectory()).buildDirectory();
            LOG.info("Creating directory: " + buildDirectory);
            try {
                File dir = Path.of(mod.getDirectory().toPath().toAbsolutePath().toString(), buildDirectory).toAbsolutePath().toFile();
                if (!dir.isDirectory()) {
                    if (!dir.mkdirs()) {
                        throw new CommitStepException("Failed to create directory - " + buildDirectory);
                    }
                }
            }
            catch (RuntimeException e) {
                LOG.error("Failed to create " + buildDirectory, e);
                throw new CommitStepException("Failed to create directory " + buildDirectory);
            }
        }
    }

    /**
     * Validates user input before {@link #updateDataModel()} is called.
     *
     * @return {@code true} if input is valid, {@code false} otherwise
     * @throws ConfigurationException if input is not valid and needs user attention. Exception message will be displayed to user
     */
    @Override
    public boolean validate() throws ConfigurationException {
        LOG.info("Validating");
        return configurable.validate();
    }

    /** Commits data from UI into ModuleBuilder and WizardContext */
    @Override
    public void updateDataModel() {
        LOG.info("updateDataModel");

        DetectedRootFacetSettings rootSettings = configurable.currentState();
        // Library projects - nothing needed
        //...
        // Root directories
        detector.configure(builder, rootSettings);
    }

}
