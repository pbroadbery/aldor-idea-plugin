package aldor.sdk;

import com.intellij.openapi.projectRoots.SdkTypeId;

// TODO: Rename to SdkType
public interface AxiomSdk extends SdkTypeId {
    boolean isLocalInstall();
}
