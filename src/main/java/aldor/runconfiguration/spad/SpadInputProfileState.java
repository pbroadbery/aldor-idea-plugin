package aldor.runconfiguration.spad;

import aldor.sdk.SdkTypes;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * ProfileState - constructs process
 */
class SpadInputProfileState extends CommandLineState {
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
            return new GeneralCommandLine().withExePath("missing-fricas-library");
        }
        if (sdk.getHomePath() == null) {
            return new GeneralCommandLine().withExePath("missing-fricas-home-path");
        }
        if (SdkTypes.fricasEnvVar(sdk) == null) {
            return new GeneralCommandLine().withExePath("missing-fricas-environment-var");
        }
        String execPath = SdkTypes.axiomSysPath(sdk);
        if (execPath == null) {
            return new GeneralCommandLine().withExePath("error");
        }
        GeneralCommandLine commandLine = SpadInputProcesses.executionCommandLine(configuration.bean(), execPath);

        commandLine.withEnvironment(SdkTypes.fricasEnvVar(sdk), sdk.getHomePath());
        commandLine.setWorkDirectory(new File(configuration.inputFile()).getParent());
        return commandLine;
    }

    @Nullable
    private String findExecutablePath() {
        Sdk sdk = configuration.effectiveSdk();
        if (sdk == null) {
            return null;
        } else {
            return SdkTypes.axiomSysPath(sdk);
        }
    }
}
