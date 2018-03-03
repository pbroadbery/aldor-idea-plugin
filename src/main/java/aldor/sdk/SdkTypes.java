package aldor.sdk;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;

import javax.annotation.Nullable;
import java.util.Optional;

public final class SdkTypes {

    @Nullable
    public static String axiomSysPath(Sdk sdk) {
        return Optional.ofNullable(sdk.getHomePath()).map(p -> p +"/bin/AXIOMsys").orElse(null);
    }

    public static boolean isLocalSdk(Sdk sdk) {
        SdkTypeId type = sdk.getSdkType();
        return ((type instanceof AxiomSdk) && ((AxiomSdk) type).isLocalInstall());
    }
}
