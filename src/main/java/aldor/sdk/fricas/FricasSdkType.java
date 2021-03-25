package aldor.sdk.fricas;

import aldor.sdk.AxiomSdk;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public interface FricasSdkType extends AxiomSdk {
    @Nullable
    String fricasPath(Sdk sdk);

    @Nonnull
    String fricasSysName(Sdk sdk);

    String fricasEnvVar();
}
