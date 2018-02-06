package aldor.spad.runconfiguration;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.openapi.projectRoots.Sdk;

public interface SpadRunProfile extends RunProfile {
    boolean isRunnable();

    Sdk configuredSdk();
}
