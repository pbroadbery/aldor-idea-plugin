package aldor.sdk.aldor;

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class AldorSdkDataConfigurable implements AdditionalDataConfigurable {
    private final AldorSdkAldorUnitForm form;
    private final AldorSdkAdditionalData bean = new AldorSdkAdditionalData();
    private Sdk currentSdk = null;

    AldorSdkDataConfigurable(SdkModel sdkModel, SdkModificator sdkModificator) {
        this.form = new AldorSdkAldorUnitForm();
    }

    @Override
    public void setSdk(Sdk sdk) {
        this.currentSdk = sdk;
        SdkAdditionalData additional = sdk.getSdkAdditionalData();
        if (!(additional instanceof AldorSdkAdditionalData)) {
            return;
        }
        AldorSdkAdditionalData aldorSdkAdditional = (AldorSdkAdditionalData) additional;
        this.bean.aldorUnitSdk = aldorSdkAdditional.aldorUnitSdk;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return form.wholePanel();
    }

    @Override
    public boolean isModified() {
        AldorSdkAdditionalData current = new AldorSdkAdditionalData();
        form.saveSettings(current);
        return !current.matches(bean);
    }

    @Override
    public void apply() {
        form.saveSettings(this.bean);
        SdkAdditionalData data = currentSdk.getSdkAdditionalData();
        this.bean.copyInfo(data);
    }

    @Override
    public void reset() {
        form.reset(this.bean);
    }

}
