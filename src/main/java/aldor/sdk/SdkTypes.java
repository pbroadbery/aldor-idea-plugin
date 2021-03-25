package aldor.sdk;

import aldor.sdk.fricas.FricasSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class SdkTypes {

    @Nullable
    public static String axiomSysPath(@NotNull Sdk sdk) {
        if (sdk.getSdkType() instanceof FricasSdkType) {
            FricasSdkType sdkType = (FricasSdkType) sdk.getSdkType();
            return Optional.ofNullable(sdk.getHomePath()).map(p -> p + "/bin/" + sdkType.fricasSysName(sdk)).orElse(null);
        }
        return null;
    }

    @Nullable
    public static VirtualFile algebraPath(@NotNull Sdk sdk) {
        return Optional.ofNullable(sdk.getHomeDirectory()).map(p -> p.findFileByRelativePath("algebra")).orElse(null);
    }


    public static boolean isLocalSdk(@NotNull Sdk sdk) {
        SdkTypeId type = sdk.getSdkType();
        return ((type instanceof AxiomSdk) && ((AxiomSdk) type).isLocalInstall());
    }

    public static boolean isAxiomSdk(@Nullable Sdk sdk) {
        if (sdk == null) {
            return false;
        }
        return sdk.getSdkType() instanceof AxiomSdk;
    }

    public static @Nullable String fricasEnvVar(Sdk sdk) {
        if (sdk.getSdkType() instanceof FricasSdkType) {
            FricasSdkType sdkType = (FricasSdkType) sdk.getSdkType();
            return sdkType.fricasEnvVar();
        }
        return null;
    }
}
