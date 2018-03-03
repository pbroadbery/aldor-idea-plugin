package aldor.test_util;

import aldor.build.module.AldorModuleType;
import aldor.sdk.AldorInstalledSdkType;
import aldor.sdk.AldorLocalSdkType;
import aldor.sdk.FricasInstalledSdkType;
import aldor.sdk.FricasLocalSdkType;
import aldor.sdk.SdkTypes;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
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
        Aldor(new AldorInstalledSdkType()),
        FricasLocal(new FricasLocalSdkType()),
        AldorLocal(new AldorLocalSdkType());

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

    public static LightProjectDescriptor fricasLocalSdkProjectDescriptor(String prefix) {
        return instance.getProjectDescriptor(SdkOption.FricasLocal, prefix);
    }

    public static LightProjectDescriptor aldorLocalSdkProjectDescriptor(String prefix) {
        return instance.getProjectDescriptor(SdkOption.AldorLocal, prefix);
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

        static Module lastModule;

        @Override
        protected void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
            super.configureModule(module, model, contentEntry);
            System.out.println("Configuring module " + module + " " + model);
            if (SdkTypes.isLocalSdk(sdk)) {
                ContentEntry newContentEntry = model.addContentEntry("file://" + prefix);
                newContentEntry.addSourceFolder("file://" + prefix +"/fricas/src", false);
                CompilerModuleExtension moduleExtension = model.getModuleExtension(CompilerModuleExtension.class);
                moduleExtension.inheritCompilerOutputPath(false);
                moduleExtension.setCompilerOutputPath("file://" + prefix + "/build");
            }
            lastModule = module;
        }

        @Override
        protected Module createModule(@NotNull Project project, @NotNull String moduleFilePath) {
            return super.createModule(project, moduleFilePath);
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
