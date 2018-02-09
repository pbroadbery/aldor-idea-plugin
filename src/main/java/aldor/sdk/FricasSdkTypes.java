package aldor.sdk;

import com.intellij.openapi.projectRoots.Sdk;

import javax.annotation.Nullable;
import java.util.Optional;

public final class FricasSdkTypes {

    @Nullable
    public static String axiomSysPath(Sdk sdk) {
        return Optional.ofNullable(sdk.getHomePath()).map(p -> p +"/bin/AXIOMsys").orElse(null);
    }
}
