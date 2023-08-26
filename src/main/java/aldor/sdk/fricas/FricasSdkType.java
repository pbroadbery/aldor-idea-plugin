package aldor.sdk.fricas;

import aldor.sdk.AxiomSdk;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

public interface FricasSdkType extends AxiomSdk {
    @NotNull
    String fricasPath(Sdk sdk);

    @NotNull
    String fricasSysName(Sdk sdk);

    String fricasEnvVar();
}
