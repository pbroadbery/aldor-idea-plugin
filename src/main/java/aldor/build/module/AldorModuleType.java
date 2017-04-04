package aldor.build.module;

import aldor.sdk.AldorSdkType;
import aldor.sdk.FricasSdkType;
import aldor.ui.AldorIcons;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

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
    public Icon getBigIcon() {
        return AldorIcons.MODULE;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean isOpened) {
        return AldorIcons.MODULE;
    }

    public static AldorModuleType instance() {
        return instance;
    }

    public static class AldorModuleBuilder extends ModuleBuilder {

        private final AldorModuleType type;

        protected AldorModuleBuilder(AldorModuleType type) {
            this.type = type;
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
            return (sdkType == AldorSdkType.instance()) || (sdkType == FricasSdkType.instance());
        }
    }
}
