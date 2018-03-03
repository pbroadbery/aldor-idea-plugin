package aldor.sdk;

import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

public interface FricasSdkType extends AxiomSdk {
    @Nullable
    String fricasPath(Sdk sdk);
}
