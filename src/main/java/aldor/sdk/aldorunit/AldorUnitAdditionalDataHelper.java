package aldor.sdk.aldorunit;

import aldor.sdk.NamedSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public final class AldorUnitAdditionalDataHelper {
    public static final AldorUnitAdditionalDataHelper instance = new AldorUnitAdditionalDataHelper();

    private AldorUnitAdditionalDataHelper() {}

    public static AldorUnitAdditionalDataHelper instance() {
        return instance;
    }

    public AldorUnitSdkDataConfigurable createAdditionalDataConfigurable() {
        return new AldorUnitSdkDataConfigurable();
    }

    public void saveAdditionalData(@NotNull SdkAdditionalData data, @NotNull Element additional) {
        AldorUnitAdditionalData additionalData = (AldorUnitAdditionalData) data;
        if ((additionalData.jdk.name() != null))
            additional.setAttribute("AldorUnitJdk", additionalData.jdk.name());
    }

    @NotNull
    public SdkAdditionalData loadAdditionalData(Element additional) {
        AldorUnitAdditionalData additionalData = new AldorUnitAdditionalData();
        String jdkName = additional.getAttributeValue("AldorUnitJdk");
        Optional<Sdk> jdk = Arrays.stream(ProjectJdkTable.getInstance().getAllJdks()).filter(sdk -> sdk.getName().equals(jdkName)).findFirst();
        additionalData.jdk = jdk.map(NamedSdk::new).orElse(new NamedSdk(jdkName));
        return additionalData;
    }

}

