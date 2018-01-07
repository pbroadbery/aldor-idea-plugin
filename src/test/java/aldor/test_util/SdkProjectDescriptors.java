package aldor.test_util;

import aldor.build.module.AldorModuleType;
import aldor.sdk.AldorInstalledSdkType;
import aldor.sdk.FricasInstalledSdkType;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SdkProjectDescriptors {
    private static final SdkProjectDescriptors instance = new SdkProjectDescriptors();
    private final Map<String, LightProjectDescriptor> descriptorForPrefix;

    public SdkProjectDescriptors() {
        descriptorForPrefix = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private enum SdkOption {

        Fricas(new FricasInstalledSdkType()),
        Aldor(new AldorInstalledSdkType());

        private final SdkType sdkType;

        SdkOption(SdkType type) {
            this.sdkType = type;
        }

        public SdkType sdkType() {
            return sdkType;
        }
    }

    public static LightProjectDescriptor fricasSdkProjectDescriptor(String prefix) {
        return instance.getProjectDescriptor(SdkOption.Fricas, prefix);
    }

    public static LightProjectDescriptor aldorSdkProjectDescriptor(String prefix) {
        return instance.getProjectDescriptor(SdkOption.Aldor, prefix);
    }

    private LightProjectDescriptor getProjectDescriptor(SdkOption sdkOption, String prefix) {
        return descriptorForPrefix.computeIfAbsent(prefix, k -> new SdkLightProjectDescriptor(sdkOption.sdkType, prefix));
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

        @Override
        public void setUpProject(@NotNull Project project, @NotNull SetupHandler handler) throws Exception {
            WriteAction.run( () -> ProjectRootManager.getInstance(project).setProjectSdk(getSdk()));
            super.setUpProject(project, handler);
        }

        @Nullable
        @Override
        public Sdk getSdk() {
            if (sdk == null) {
                sdk = createSDK();
                ProjectJdkTable.getInstance().addJdk(sdk);
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
