package aldor.sdk;

import javax.annotation.Nonnull;

public interface AxiomInstalledSdk extends AxiomSdk {
    @Nonnull String librarySuffix();
}
