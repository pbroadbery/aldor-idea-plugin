package aldor.runconfiguration.spad;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

public interface SpadRunProfile extends RunProfile {
    boolean isRunnable();

    @Nullable
    Sdk configuredSdk();
}
