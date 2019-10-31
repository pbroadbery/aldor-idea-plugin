package aldor.sdk.aldor;

import aldor.sdk.NamedSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class AldorSdkAdditionalDataHelper {
    public static final AldorSdkAdditionalDataHelper instance = new AldorSdkAdditionalDataHelper();

    private AldorSdkAdditionalDataHelper() {}

    public static AldorSdkAdditionalDataHelper instance() {
        return instance;
    }

    AldorSdkAdditionalData additionalData(Sdk sdk) {
        return (AldorSdkAdditionalData) sdk.getSdkAdditionalData();
    }

    @Nullable
    public AldorSdkDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return new AldorSdkDataConfigurable(sdkModel, sdkModificator);
    }

    public void saveAdditionalData(@NotNull SdkAdditionalData data, @NotNull Element additional) {
        AldorSdkAdditionalData aldorSdkAdditionalData = (AldorSdkAdditionalData) data;
        if (aldorSdkAdditionalData.aldorUnitSdk.name() != null)
            additional.setAttribute("AldorUnitSdkName", aldorSdkAdditionalData.aldorUnitSdk.name());
        additional.setAttribute("AldorUnitEnabled", Boolean.toString(aldorSdkAdditionalData.aldorUnitEnabled));
        additional.setAttribute("JavaClassDirectory", Optional.ofNullable(aldorSdkAdditionalData.javaClassDirectory).orElse(""));
    }

    @Nullable
    public SdkAdditionalData loadAdditionalData(Element additional) {
        AldorSdkAdditionalData data = new AldorSdkAdditionalData();
        String sdkName = additional.getAttributeValue("AldorUnitSdkName");
        Optional<Sdk> sdk = Arrays.stream(ProjectJdkTable.getInstance().getAllJdks()).filter(s -> s.getName().equals(sdkName)).findFirst();
        data.aldorUnitSdk= sdk.map(NamedSdk::new).orElse(new NamedSdk(sdkName));
        data.aldorUnitEnabled = Boolean.valueOf(additional.getAttributeValue("AldorUnitEnabled"));
        data.javaClassDirectory = additional.getAttributeValue("JavaClassDirectory");
        return data;
    }

}
