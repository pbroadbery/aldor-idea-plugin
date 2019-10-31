package aldor.runconfiguration.aldor;

import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class AldorRunConfigurationModule extends RunConfigurationModule {
    public AldorRunConfigurationModule(@NotNull Project project) {
        super(project);
    }
}
