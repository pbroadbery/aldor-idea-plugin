package aldor.sdk;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class SdkTypes {

    @Nullable
    public static String axiomSysPath(@NotNull Sdk sdk) {
        return Optional.ofNullable(sdk.getHomePath()).map(p -> p +"/bin/AXIOMsys").orElse(null);
    }

    @Nullable
    public static VirtualFile algebraPath(@NotNull Sdk sdk) {
        return Optional.ofNullable(sdk.getHomeDirectory()).map(p -> p.findFileByRelativePath("algebra")).orElse(null);
    }


    public static boolean isLocalSdk(@NotNull Sdk sdk) {
        SdkTypeId type = sdk.getSdkType();
        return ((type instanceof AxiomSdk) && ((AxiomSdk) type).isLocalInstall());
    }
}
