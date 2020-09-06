package aldor.sdk.aldor;

import aldor.sdk.AxiomSdk;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

// TODO: Rename to SDK type
public interface AldorSdkType extends AxiomSdk {
    @Nullable
    String aldorPath(Sdk sdk);
}
