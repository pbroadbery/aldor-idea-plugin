package aldor.runconfiguration.spad;

import aldor.sdk.SdkTypes;
import aldor.sdk.fricas.FricasSdkType;
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
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
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
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class SpadInputRunConfigurationType extends ConfigurationTypeBase {

    protected SpadInputRunConfigurationType() {
        super("SpadInputRunConfigurationType", "Spad Input File", "Execute a Spad .input file", AldorIcons.FILE);
        //noinspection ThisEscapedInObjectConstruction
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
    public static class SpadInputConfiguration extends ModuleBasedConfiguration<RunConfigurationModule, Element> implements SpadRunProfile, RunConfigurationWithSuppressedDefaultDebugAction {
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

        private static final String SPAD_INPUT_CONF_ELT = "SpadInput";
        private static final String SPAD_INPUT_FILE = "inputFile";
        private static final String SPAD_LOAD_LOCAL = "loadLocal";
        private static final String SPAD_KEEP_RUNNING = "keepRunning";

        @SuppressWarnings("ThrowsRuntimeException")
        @Override
        public void writeExternal(@NotNull Element parentElement) throws WriteExternalException {
            super.writeExternal(parentElement);
            final Element element = new Element(SPAD_INPUT_CONF_ELT);
            parentElement.addContent(element);
            element.setAttribute(SPAD_INPUT_FILE, (bean.inputFile == null) ? "" : bean.inputFile);
            element.setAttribute(SPAD_LOAD_LOCAL, Boolean.toString(bean.loadSpad));
            element.setAttribute(SPAD_KEEP_RUNNING, Boolean.toString(bean.keepRunning));
        }

        @SuppressWarnings("ThrowsRuntimeException")
        @Override
        public void readExternal(@NotNull Element parentElement) throws InvalidDataException {
            super.readExternal(parentElement);
            final Element element = parentElement.getChild(SPAD_INPUT_CONF_ELT);
            if (element == null) {
                throw new InvalidDataException("Missing configuration element");
            }
            this.bean.inputFile = element.getAttributeValue(SPAD_INPUT_FILE);
            this.bean.loadSpad = Boolean.parseBoolean(element.getAttributeValue(SPAD_LOAD_LOCAL));
            this.bean.keepRunning= Boolean.parseBoolean(element.getAttributeValue(SPAD_KEEP_RUNNING));
        }
    }

    public static class SpadInputConfigurationBean {
        public String inputFile = "";
        public boolean loadSpad = false;
        public boolean keepRunning = false;
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

        protected GeneralCommandLine createCommandLine() {
            Sdk sdk = configuration.effectiveSdk();
            if (sdk == null) {
                return new GeneralCommandLine().withExePath("missing-sdk");
            }
            if (sdk.getHomePath() == null) {
                return new GeneralCommandLine().withExePath("missing-sdk-home-path");
            }
            String execPath = SdkTypes.axiomSysPath(sdk);
            if (execPath == null) {
                return new GeneralCommandLine().withExePath("error");
            }
            GeneralCommandLine commandLine = new GeneralCommandLine().withExePath(execPath);
            commandLine.addParameter("-eval");
            commandLine.addParameter(")r " + configuration.inputFile());
            commandLine.addParameter("-eval");
            if (!configuration.bean().keepRunning) {
                commandLine.addParameter(")q");
            }
            commandLine.withEnvironment("AXIOM", sdk.getHomePath());
            commandLine.setWorkDirectory(new File(configuration.inputFile()).getParent());
            return commandLine;
        }

        @Nullable
        private String findExecutablePath() {
            Sdk sdk = configuration.effectiveSdk();
            if (sdk == null) {
                return null;
            }
            else {
                return SdkTypes.axiomSysPath(sdk);
            }
        }
    }

}
