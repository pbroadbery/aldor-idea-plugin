package aldor.sdk.aldor;

import com.google.common.collect.Lists;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.util.Consumer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.util.List;

public class AldorLocalSdkType extends SdkType implements AldorSdkType {
    public static List<String> ALDOR_SOURCE_DIRS = Lists.newArrayList("aldor/aldor/lib/libfoam/al", "aldor/lib/aldor", "aldor/lib/algebra", "aldor/lib/libaxllib");
    public static List<String> ALDOR_TEST_DIRS = Lists.newArrayList("aldor/aldor/test");
    private final AldorSdkAdditionalDataHelper additionalDataHelper = AldorSdkAdditionalDataHelper.instance();

    public static AldorLocalSdkType instance() {
        return findInstance(AldorLocalSdkType.class);
    }

    public AldorLocalSdkType() {
        super("Aldor Local SDK");
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        return null;
    }

    @Override
    public boolean isValidSdkHome(String path) {
        return true;
    }

    @NotNull
    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        return currentSdkName;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Local Aldor SDK";
    }

    @Nullable
    @Override
    public String aldorPath(Sdk sdk) {
        return null;
    }

    @Override
    public boolean isLocalInstall() {
        return true;
    }

    @Override
    public boolean supportsCustomCreateUI() {
        return true;
    }

    @Override
    public void showCustomCreateUI(@NotNull SdkModel sdkModel, @NotNull JComponent parentComponent, Sdk parentSdk,
                                   @NotNull Consumer<? super Sdk> sdkCreatedCallback) {
        ProjectJdkImpl sdk = new ProjectJdkImpl("Local Aldor Build", this);
        sdk.setVersionString("git repository");
        sdkCreatedCallback.consume(sdk);
    }

    private AldorSdkAdditionalData additionalData(Sdk sdk) {
        return additionalDataHelper.additionalData(sdk);
    }

    @Nullable
    @Override
    public AldorSdkDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return additionalDataHelper.createAdditionalDataConfigurable(sdkModel, sdkModificator);
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {
        additionalDataHelper.saveAdditionalData(additionalData, additional);
    }

    @Override
    @Nullable
    public SdkAdditionalData loadAdditionalData(Element additional) {
        return additionalDataHelper.loadAdditionalData(additional);
    }
}
