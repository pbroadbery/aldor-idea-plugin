package aldor.sdk;

import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

public interface FricasSdkType {
    @Nullable
    String fricasPath(Sdk sdk);

    boolean isLocalInstall();
}
