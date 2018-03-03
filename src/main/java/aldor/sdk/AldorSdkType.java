package aldor.sdk;

import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

public interface AldorSdkType extends AxiomSdk {
    @Nullable
    String aldorPath(Sdk sdk);
}
