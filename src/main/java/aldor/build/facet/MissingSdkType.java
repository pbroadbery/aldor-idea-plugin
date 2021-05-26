package aldor.build.facet;

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MissingSdkType extends SdkType {
    private final SdkType sdkType;

    public MissingSdkType(SdkType type) {
        super("Missing " + type.getName());
        this.sdkType = type;
    }

    @Override
    @Nullable
    public String suggestHomePath() {
        return null;
    }

    @Override
    public boolean isValidSdkHome(@NotNull String path) {
        return false;
    }

    @Override
    @NotNull
    public String suggestSdkName(@Nullable String currentSdkName, @NotNull String sdkHome) {
        throw new UnsupportedOperationException("suggest sdk name");
    }

    @Override
    @Nullable
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return null;
    }

    @Override
    @NotNull
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getPresentableName() {
        return getName();
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {
    }
}
