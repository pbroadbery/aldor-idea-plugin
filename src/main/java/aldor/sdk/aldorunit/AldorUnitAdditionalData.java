package aldor.sdk.aldorunit;

import aldor.sdk.NamedSdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AldorUnitAdditionalData implements SdkAdditionalData {
    @NotNull
    public NamedSdk jdk = new NamedSdk((String) null);
    boolean isSomething = false;

    public boolean matches(AldorUnitAdditionalData other) {
        return Objects.equals(this.jdk, other.jdk) && (this.isSomething == other.isSomething);
    }

    public void copyInto(AldorUnitAdditionalData given) {
        given.jdk = this.jdk;
        given.isSomething = this.isSomething;
    }
}
