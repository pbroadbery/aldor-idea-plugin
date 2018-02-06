package aldor.spad.runconfiguration;

import aldor.sdk.FricasSdkType;
import aldor.ui.AldorIcons;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class SpadInputRunConfigurationType extends ConfigurationTypeBase {

    protected SpadInputRunConfigurationType() {
        super("SpadInputRunConfigurationType", "Spad Input File", "Execute a Spad .input file", AldorIcons.FILE);
        addFactory(new SpadInputConfigurationFactory(this));
    }

    private static class SpadInputConfigurationFactory extends ConfigurationFactory {

        protected SpadInputConfigurationFactory(@NotNull ConfigurationType type) {
            super(type);
        }

        @NotNull
        @Override
        public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
            return new SpadInputConfiguration(new RunConfigurationModule(project), this);
        }
    }

    /**
     * Configuration - the settings (file name, how to run, etc)
     */
    public static class SpadInputConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> implements SpadRunProfile {
        private final SpadInputConfigurationBean bean = new SpadInputConfigurationBean();

        public SpadInputConfiguration(String name, @NotNull RunConfigurationModule configurationModule, @NotNull ConfigurationFactory factory) {
            super(name, configurationModule, factory);
        }

        public SpadInputConfiguration(RunConfigurationModule configurationModule, ConfigurationFactory factory) {
            this("", configurationModule, factory);
        }

        public SpadInputConfigurationBean bean() {
            return bean;
        }

        @Override
        public Collection<Module> getValidModules() {
            // TODO: Filter by type
            return Arrays.asList(ModuleManager.getInstance(getProject()).getModules());
        }

        @NotNull
        @Override
        public SettingsEditor<SpadInputConfiguration> getConfigurationEditor() {
            SettingsEditorGroup<SpadInputConfiguration> group = new SettingsEditorGroup<>();

            group.addEditor("Run details", new SpadInputConfigurable(getProject()));

            return group;
        }

        @Nullable
        @Override
        public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
            return new SpadInputProfileState(this, environment);
        }

        public void inputFile(String inputFile) {
            this.bean.inputFile = inputFile;
        }

        public String inputFile() {
            return bean.inputFile;
        }

        @Override
        public boolean isRunnable() {
            return true;
        }

        @Override
        public Sdk configuredSdk() {
            // Ideally, we'd check with the form...
            return ProjectRootManager.getInstance(getProject()).getProjectSdk();
        }

        @Nullable
        public Sdk effectiveSdk() {
            Sdk mySdk = configuredSdk();
            if (mySdk.getSdkType() instanceof FricasSdkType) {
                return mySdk;
            }
            else {
                for (Sdk sdk: ProjectJdkTable.getInstance().getAllJdks()) {
                    if (sdk.getSdkType() instanceof FricasSdkType) {
                        return sdk;
                    }
                }
            }
            return null;

        }

    }

    @SuppressWarnings({"InstanceVariableMayNotBeInitialized", "PublicField"})
    public static class SpadInputConfigurationBean {
        public String inputFile = "";
        public boolean loadSpad = false;
        public boolean keep;
        public Module module;
    }

    /**
     * ProfileState - constructs process
     */
    private static class SpadInputProfileState extends CommandLineState {
        private static final Logger LOG = Logger.getInstance(SpadInputProfileState.class);
        private final SpadInputConfiguration configuration;

        protected SpadInputProfileState(SpadInputConfiguration configuration, ExecutionEnvironment environment) {
            super(environment);
            this.configuration = configuration;
        }

        @NotNull
        @Override
        protected ProcessHandler startProcess() throws ExecutionException {
            return startProcess(createCommandLine());
        }

        static OSProcessHandler startProcess(GeneralCommandLine commandLine) throws ExecutionException {
            ProcessHandlerFactory factory = ProcessHandlerFactory.getInstance();
            OSProcessHandler processHandler = factory.createColoredProcessHandler(commandLine);
            ProcessTerminatedListener.attach(processHandler);

            return processHandler;
        }

        protected GeneralCommandLine createCommandLine() throws ExecutionException {
            Sdk sdk = configuration.effectiveSdk();
            if (sdk == null) {
                return new GeneralCommandLine().withExePath("missing-sdk");
            }
            String execPath = findExecutablePath();
            GeneralCommandLine commandLine = new GeneralCommandLine().withExePath(execPath);
            commandLine.addParameter("-eval");
            commandLine.addParameter(")r " + configuration.inputFile());
            commandLine.addParameter("-eval");
            commandLine.addParameter(")q");
            commandLine.withEnvironment("AXIOM", sdk.getHomePath());
            LOG.info("Environment: " + commandLine.getEffectiveEnvironment());
            return commandLine;
        }

        private String findExecutablePath() {
            return configuration.effectiveSdk().getHomePath() +"/bin/AXIOMsys";
        }
    }

}
