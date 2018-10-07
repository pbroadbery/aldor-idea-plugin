package aldor.build.module;

import aldor.sdk.AldorInstalledSdkType;
import aldor.sdk.FricasInstalledSdkType;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.ProjectType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;

public class AldorModuleBuilder extends ModuleBuilder {
    public static final ProjectType ALDOR_PROJECT_TYPE = new ProjectType("Aldor/Spad");

    private final ModuleType<?> type;

    protected AldorModuleBuilder(ModuleType<?> type) {
        this.type = type;
    }

    @Override
    protected ProjectType getProjectType() {
        return ALDOR_PROJECT_TYPE;
    }

    @Override
    protected boolean isAvailable() {
        return false;
    }

    @Override
    public void setupRootModel(final ModifiableRootModel rootModel) throws ConfigurationException {
        // false for the module automatically created in a new project
        if (myJdk != null) {
            rootModel.setSdk(myJdk);
        }
        else {
            rootModel.inheritSdk();
        }

        doAddContentEntry(rootModel);
    }

    @Override
    public ModuleType<?> getModuleType() {
        return type;
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        //noinspection ObjectEquality
        return (sdkType == AldorInstalledSdkType.instance()) || (sdkType == FricasInstalledSdkType.instance());
    }
}
