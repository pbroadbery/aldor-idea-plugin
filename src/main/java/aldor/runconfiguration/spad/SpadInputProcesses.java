package aldor.runconfiguration.spad;

import com.intellij.execution.configurations.GeneralCommandLine;

public class SpadInputProcesses {

    public static GeneralCommandLine executionCommandLine(SpadInputRunConfigurationType.SpadInputConfigurationBean bean, String execPath) {
        GeneralCommandLine commandLine = new GeneralCommandLine().withExePath(execPath);
        commandLine.addParameter("-eval");
        commandLine.addParameter(")r " + bean.inputFile);
        if (!bean.keepRunning) {
            commandLine.addParameter("-eval");
            commandLine.addParameter(")q");
        }
        return commandLine;
    }

}
