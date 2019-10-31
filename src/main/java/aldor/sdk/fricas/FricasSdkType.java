package aldor.sdk.fricas;

import aldor.sdk.AxiomSdk;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

public interface FricasSdkType extends AxiomSdk {
    @Nullable
    String fricasPath(Sdk sdk);
}
