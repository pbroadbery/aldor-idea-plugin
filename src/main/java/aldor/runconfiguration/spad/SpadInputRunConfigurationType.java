package aldor.runconfiguration.spad;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import icons.AldorIcons;
import org.jetbrains.annotations.NotNull;

public class SpadInputRunConfigurationType extends SimpleConfigurationType {
    @NotNull
    public static SpadInputRunConfigurationType instance() {
        return CONFIGURATION_TYPE_EP.findExtensionOrFail(SpadInputRunConfigurationType.class);
    }

    protected SpadInputRunConfigurationType() {
        super("SpadInputRunConfigurationType", "Spad Input File", "Execute a Spad .input file",
                NotNullLazyValue.createValue(() -> AldorIcons.SPAD_INPUT_FILE));
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new SpadInputConfiguration(new RunConfigurationModule(project), this);
    }

    public static class SpadInputConfigurationBean {
        public String inputFile = "";
        public boolean loadSpad = false;
        public boolean keepRunning = false;
    }

}
