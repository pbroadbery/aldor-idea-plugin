package aldor.build.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.ProjectType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

public class AldorModuleBuilder extends ModuleBuilder {
    public static final ProjectType ALDOR_PROJECT_TYPE = new ProjectType("Aldor & Fricas");

    private final ModuleType<?> type;

    protected AldorModuleBuilder(ModuleType<?> type) {
        this.type = type;
    }

    @Override
    protected ProjectType getProjectType() {
        return ALDOR_PROJECT_TYPE;
    }

    @Override
    public void setupRootModel(@NotNull final ModifiableRootModel rootModel) throws ConfigurationException {
        doAddContentEntry(rootModel);
    }

    @Override
    public ModuleType<?> getModuleType() {
        return type;
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        return true;
    }

}
