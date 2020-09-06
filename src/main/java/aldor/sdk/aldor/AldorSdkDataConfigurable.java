package aldor.sdk.aldor;

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class AldorSdkDataConfigurable implements AdditionalDataConfigurable {
    private final AldorSdkAdditionalData bean = new AldorSdkAdditionalData();
    private Sdk currentSdk = null;

    AldorSdkDataConfigurable(SdkModel sdkModel, SdkModificator sdkModificator) {
    }

    @Override
    public void setSdk(Sdk sdk) {
        this.currentSdk = sdk;
        SdkAdditionalData additional = sdk.getSdkAdditionalData();
        if (!(additional instanceof AldorSdkAdditionalData)) {
            return;
        }
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {
    }

    @Override
    public void reset() {
    }

}
