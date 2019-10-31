package aldor.sdk.aldor;

import aldor.sdk.NamedSdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;

import java.util.Objects;

public class AldorSdkAdditionalData implements SdkAdditionalData {
    public boolean aldorUnitEnabled = true;
    public NamedSdk aldorUnitSdk = new NamedSdk((String) null);
    public String javaClassDirectory = null;

    public boolean matches(AldorSdkAdditionalData data) {
        if (aldorUnitEnabled != data.aldorUnitEnabled) {
            return false;
        }
        if (!Objects.equals(aldorUnitSdk, data.aldorUnitSdk)) {
            return false;
        }
        if (!Objects.equals(javaClassDirectory, data.javaClassDirectory)) {
            return false;
        }
        return true;
    }

    public void copyInfo(SdkAdditionalData data) {
        AldorSdkAdditionalData other = (AldorSdkAdditionalData) data;
        other.aldorUnitSdk = aldorUnitSdk;
        other.aldorUnitEnabled = aldorUnitEnabled;
        other.javaClassDirectory = javaClassDirectory;
    }
}