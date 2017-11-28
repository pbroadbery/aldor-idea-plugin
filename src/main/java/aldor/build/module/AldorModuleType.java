package aldor.build.module;

import aldor.sdk.AldorInstalledSdkType;
import aldor.sdk.FricasInstalledSdkType;
import aldor.ui.AldorIcons;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.ProjectType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.Icon;

/**
 * Represents an aldor module.
 */
public class AldorModuleType extends ModuleType<AldorModuleType.AldorModuleBuilder> {
    public static final String NAME = "Aldor Module";
    public static final String ID = "ALDOR-MODULE";
    private static final AldorModuleType instance = new AldorModuleType();

    public AldorModuleType() {
        super(ID);
    }

    @NotNull
    @Override
    public AldorModuleBuilder createModuleBuilder() {
        return new AldorModuleBuilder(this);
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Module containing spad/aldor sources";
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean isOpened) {
        return AldorIcons.MODULE;
    }

    public static AldorModuleType instance() {
        return instance;
    }

    @Override
    public boolean isSupportedRootType(@SuppressWarnings("rawtypes") JpsModuleSourceRootType type) {
        return type == JavaSourceRootType.SOURCE;
    }

    public static class AldorModuleBuilder extends ModuleBuilder {
        public static final ProjectType ALDOR_PROJECT_TYPE = new ProjectType("Aldor/Spad");

        private final AldorModuleType type;

        protected AldorModuleBuilder(AldorModuleType type) {
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
}
