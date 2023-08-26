package aldor.sdk.fricas;

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

public class FricasLocalSdkType extends SdkType implements FricasSdkType {

    public FricasLocalSdkType() {
        super("Fricas Local SDK");
    }

    @Override
    public @NotNull String fricasPath(Sdk sdk) {
        return null;
    }

    @Override
    public @NotNull String fricasSysName(Sdk sdk) {
        return "FRICASsys";
    }

    @Override
    public String fricasEnvVar() {
        return "FRICAS";
    }

    @Override
    public boolean isLocalInstall() {
        return true;
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        return null;
    }

    @Override
    public boolean isValidSdkHome(String path) {
        return false;
    }

    @NotNull
    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        return "Local Fricas";
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return new FricasSdkDataConfigurable();
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Local Fricas";
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {
    }

    @Override
    public boolean isLocalSdk(@NotNull Sdk sdk) {
        return false;
    }


    @Override
    public boolean supportsCustomCreateUI() {
        return true;
    }

    @Override
    public void showCustomCreateUI(@NotNull SdkModel sdkModel, @NotNull JComponent parentComponent,
                                   Sdk parentSdk, @NotNull Consumer<? super Sdk> sdkCreatedCallback) {
        ProjectJdkImpl sdk = new ProjectJdkImpl("Local Fricas Build", this);
        sdk.setVersionString("git repository");
        sdkCreatedCallback.consume(sdk);
        /*
        /home/pab/Work/IdeaProjects/week1/aldorparse_19/aldorparse/src/main/java/aldor/sdk/fricas/FricasLocalSdkType.java:91: error: name clash:
            showCustomCreateUI(SdkModel,JComponent,Sdk,@org.jetbrains.annotations.NotNull Consumer<? super Sdk>) in
            showCustomCreateUI(SdkModel,JComponent,Sdk,Consumer<Sdk>) in SdkType have the same erasure, yet neither overrides the other

         */
    }

}
