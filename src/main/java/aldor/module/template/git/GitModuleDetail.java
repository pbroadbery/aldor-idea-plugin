package aldor.module.template.git;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;

interface GitModuleDetail {
    boolean isSuitableSdkType(SdkTypeId sdkType);

    String name();
    void setupRootModel(ModifiableRootModel model) throws ConfigurationException;

    void doClone(Project project) throws ConfigurationException;
}
