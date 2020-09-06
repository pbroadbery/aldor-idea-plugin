package aldor.sdk.aldor;

import com.intellij.openapi.projectRoots.SdkAdditionalData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AldorSdkAdditionalData implements SdkAdditionalData {
    public boolean aldorUnitEnabled = true;
    public String javaClassDirectory = null;

    public boolean matches(@NotNull AldorSdkAdditionalData data) {
        if (aldorUnitEnabled != data.aldorUnitEnabled) {
            return false;
        }
        if (!Objects.equals(javaClassDirectory, data.javaClassDirectory)) {
            return false;
        }
        return true;
    }

    public void copyInfo(SdkAdditionalData data) {
        AldorSdkAdditionalData other = (AldorSdkAdditionalData) data;
        other.aldorUnitEnabled = aldorUnitEnabled;
        other.javaClassDirectory = javaClassDirectory;
    }
}