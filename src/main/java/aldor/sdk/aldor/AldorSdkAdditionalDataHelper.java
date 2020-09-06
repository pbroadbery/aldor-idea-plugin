package aldor.sdk.aldor;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class AldorSdkAdditionalDataHelper {
    public static final AldorSdkAdditionalDataHelper instance = new AldorSdkAdditionalDataHelper();

    private AldorSdkAdditionalDataHelper() {}

    public static AldorSdkAdditionalDataHelper instance() {
        return instance;
    }

    public AldorSdkAdditionalData additionalData(Sdk sdk) {
        return (AldorSdkAdditionalData) sdk.getSdkAdditionalData();
    }

    public AldorSdkDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return new AldorSdkDataConfigurable(sdkModel, sdkModificator);
    }

    public void saveAdditionalData(@NotNull SdkAdditionalData data, @NotNull Element additional) {
        AldorSdkAdditionalData aldorSdkAdditionalData = (AldorSdkAdditionalData) data;
        additional.setAttribute("AldorUnitEnabled", Boolean.toString(aldorSdkAdditionalData.aldorUnitEnabled));
        additional.setAttribute("JavaClassDirectory", Optional.ofNullable(aldorSdkAdditionalData.javaClassDirectory).orElse(""));
    }

    public AldorSdkAdditionalData loadAdditionalData(Element additional) {
        AldorSdkAdditionalData data = new AldorSdkAdditionalData();
        data.aldorUnitEnabled = Boolean.valueOf(additional.getAttributeValue("AldorUnitEnabled"));
        data.javaClassDirectory = additional.getAttributeValue("JavaClassDirectory");
        return data;
    }

}
