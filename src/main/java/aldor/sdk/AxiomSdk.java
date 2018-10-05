package aldor.sdk;

import com.intellij.openapi.projectRoots.SdkTypeId;

public interface AxiomSdk extends SdkTypeId {
    boolean isLocalInstall();

}
