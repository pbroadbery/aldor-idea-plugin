package aldor.sdk;

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
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

public class AldorLocalSdkType extends SdkType implements AldorSdkType {

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

    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        return currentSdkName;
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return null;
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {

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
    public void showCustomCreateUI(@NotNull SdkModel sdkModel, @NotNull JComponent parentComponent, Sdk parentSdk, @NotNull Consumer<Sdk> sdkCreatedCallback) {
        ProjectJdkImpl sdk = new ProjectJdkImpl("Local Aldor Build", this);
        sdk.setVersionString("git repository");
        sdkCreatedCallback.consume(sdk);
    }
}
