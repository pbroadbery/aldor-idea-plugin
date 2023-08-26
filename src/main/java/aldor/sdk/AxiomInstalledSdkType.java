package aldor.sdk;

import org.jetbrains.annotations.NotNull;

public interface AxiomInstalledSdkType extends AxiomSdk {
    @NotNull String librarySuffix();
}
