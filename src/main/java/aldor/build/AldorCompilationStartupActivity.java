package aldor.build;

import aldor.build.module.AldorModuleType;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class AldorCompilationStartupActivity implements StartupActivity, DumbAware {
    private static final Logger LOG = Logger.getInstance(AldorCompilationStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("Starting aldor plugin - disabling compile validation");
        CompilerManager compilerManager = CompilerManager.getInstance(project);
        compilerManager.setValidationEnabled(AldorModuleType.instance(), false);
    }
}
