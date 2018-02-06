package aldor.spad.runconfiguration;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpadProgramRunner extends DefaultProgramRunner {
    private static final Logger LOG = Logger.getInstance(SpadProgramRunner.class);

    public static final String ID = "SpadRunner";

    public SpadProgramRunner() {
        LOG.info("Creating spad runner");
    }

    @NotNull
    @Override
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        LOG.info("Can run: " + executorId + " " + profile.getName());
        if (!Objects.equals(executorId, DefaultRunExecutor.EXECUTOR_ID)) {
            return false;
        }
        if (profile instanceof SpadRunProfile) {
            return ((SpadRunProfile) profile).isRunnable();
        }
        LOG.info("Can run: " + executorId + " nope");
        return false;
    }

    @Override
    public void onProcessStarted(RunnerSettings settings, ExecutionResult executionResult) {
        LOG.info("Process started: "+ settings + " "+ executionResult.getProcessHandler().isProcessTerminated());
    }
}
