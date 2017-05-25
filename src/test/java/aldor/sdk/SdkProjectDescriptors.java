package aldor.sdk;

import aldor.build.module.AldorModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SdkProjectDescriptors {

    public static LightProjectDescriptor fricasSdkProjectDescriptor(String prefix) {
        return new SdkLightProjectDescriptor(new FricasInstalledSdkType(), prefix);
    }

    public static LightProjectDescriptor aldorSdkProjectDescriptor(String prefix) {
        return new SdkLightProjectDescriptor(new AldorInstalledSdkType(), prefix);
    }

    private static class SdkLightProjectDescriptor extends LightProjectDescriptor {
        private final String prefix;
        private final SdkType sdkType;
        private Sdk sdk = null;

        SdkLightProjectDescriptor(SdkType sdkType, String prefix) {
            this.sdkType = sdkType;
            this.prefix = prefix;
        }

        @Override
        @NotNull
        public ModuleType<?> getModuleType() {
            return AldorModuleType.instance();
        }

        @Nullable
        @Override
        public Sdk getSdk() {
            if (sdk == null) {
                sdk = createSDK();
            }
            return sdk;
        }

        Sdk createSDK() {
            Sdk theSdk = new ProjectJdkImpl("Fricas Test SDK", sdkType);

            SdkModificator mod = theSdk.getSdkModificator();
            mod.setHomePath(prefix);
            mod.commitChanges();
            sdkType.setupSdkPaths(theSdk);
            return theSdk;
        }

    }
}
