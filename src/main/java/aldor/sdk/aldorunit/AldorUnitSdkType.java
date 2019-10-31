package aldor.sdk.aldorunit;

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.impl.JavaDependentSdkType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * SDK type for aldor unit.
 * Using an SDK to carry java path information, the aldorunit runner can then
 * pick up details.
 *
 */
public class AldorUnitSdkType extends JavaDependentSdkType {
    private static final AldorUnitSdkType instance = new AldorUnitSdkType();

    public AldorUnitSdkType() {
        super("AldorUnit");
    }

    public static AldorUnitSdkType instance() {
        return instance;
    }

    @Nullable
    @Override
    public String getBinPath(@NotNull Sdk sdk) {
        Sdk jdk = jdk(sdk);
        return ((JavaSdkType) Objects.requireNonNull(jdk)).getBinPath(jdk);
    }


    @Nullable
    @Override
    public String getToolsPath(@NotNull Sdk sdk) {
        Sdk jdk = jdk(sdk);
        return ((JavaSdkType) Objects.requireNonNull(jdk)).getBinPath(jdk);
    }

    @Nullable
    @Override
    public String getVMExecutablePath(@NotNull Sdk sdk) {
        Sdk jdk = jdk(sdk);
        return ((JavaSdkType) Objects.requireNonNull(jdk)).getBinPath(jdk);

    }

    @Nullable
    @Override
    public String suggestHomePath() {
        return "/home/pab/IdeaProjects/aldor-unit";
    }

    @Override
    public boolean isValidSdkHome(String path) {
        return new File(path, "aldorunit.jar").exists();
    }

    @NotNull
    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        if (isValidSdkHome(sdkHome)) {
            return "AldorUnit " + sdkHome; // Maybe add a version here
        }
        return currentSdkName;
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return new AldorUnitSdkDataConfigurable();
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return getName();
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {
        AldorUnitAdditionalDataHelper.instance().saveAdditionalData(additionalData, additional);
    }

    @Nullable
    @Override
    public SdkAdditionalData loadAdditionalData(Element additional) {
        return AldorUnitAdditionalDataHelper.instance().loadAdditionalData(additional);
    }

    @Nullable
    public Sdk jdk(Sdk aldorUnitSdk) {
        return Arrays.stream(ProjectJdkTable.getInstance().getAllJdks()).filter(x -> x.getSdkType().equals(JavaSdk.getInstance())).findFirst().orElse(null);
    }
}
