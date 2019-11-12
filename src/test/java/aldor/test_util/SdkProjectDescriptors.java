package aldor.test_util;

import aldor.build.module.AldorMakeDirectoryOption;
import aldor.build.module.AldorModulePathService;
import aldor.build.module.AldorModuleType;
import aldor.module.template.AldorSimpleModuleBuilder;
import aldor.module.template.AldorTemplateFactory;
import aldor.sdk.NamedSdk;
import aldor.sdk.aldor.AldorInstalledSdkType;
import aldor.sdk.aldor.AldorLocalSdkType;
import aldor.sdk.aldor.AldorSdkAdditionalData;
import aldor.sdk.aldorunit.AldorUnitSdkType;
import aldor.sdk.fricas.FricasInstalledSdkType;
import aldor.sdk.fricas.FricasLocalSdkType;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.CompositeDisposable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectTemplate;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SdkProjectDescriptors {
    private static final SdkProjectDescriptors instance = new SdkProjectDescriptors();
    private final Map<String, LightProjectDescriptor> descriptorForPrefix;

    public SdkProjectDescriptors() {
        descriptorForPrefix = new ConcurrentHashMap<>();
    }

    private interface SdkDescriptor {
        SdkOption sdkOption();
        String name(String prefix);

        Sdk editSdk(Sdk theSdk);
    }

    private enum SdkOption implements SdkDescriptor {

        Fricas(new FricasInstalledSdkType()),
        Aldor(new AldorInstalledSdkType()),
        FricasLocal(new FricasLocalSdkType()),
        AldorLocal(new AldorLocalSdkType());

        private final SdkType sdkType;

        SdkOption(SdkType type) {
            this.sdkType = type;
        }

        @Nullable
        public SdkType sdkType() {
            return sdkType;
        }

        @Override
        public SdkOption sdkOption() {
            return this;
        }

        @Override
        public String name(String prefix) {
            return name() + "_" + prefix;
        }

        @Override
        public Sdk editSdk(Sdk theSdk) {
            return theSdk;
        }
    }

    private static class WithAldorUnit implements SdkDescriptor {
        SdkDescriptor innerSdkDescriptor;
        String aldorUnitHomePath = "/home/pab/IdeaProjects/type-library/out/artifacts/aldorunit";
        WithAldorUnit(SdkDescriptor descriptor) {
            this.innerSdkDescriptor = descriptor;
        }

        @Override
        public SdkOption sdkOption() {
            return innerSdkDescriptor.sdkOption();
        }

        @Override
        public String name(String prefix) {
            return innerSdkDescriptor.name(prefix) + "_AldorUnit";
        }

        @Override
        public Sdk editSdk(Sdk theSdk) {
            createJDK();
            Sdk aldorUnitSdk = createAldorUnitSdk();
            AldorSdkAdditionalData additionalData = new AldorSdkAdditionalData();
            additionalData.aldorUnitSdk = new NamedSdk(aldorUnitSdk);
            additionalData.aldorUnitEnabled = true;
            SdkModificator mod = theSdk.getSdkModificator();
            mod.setSdkAdditionalData(additionalData);
            mod.commitChanges();
            return theSdk;
        }


        private Sdk createJDK() {
            Sdk jdk = JavaSdk.getInstance().createJdk("java", "/home/pab/Work/intellij/jdk1.8.0_101");
            ProjectJdkTable.getInstance().addJdk(jdk);
            return jdk;
        }


        private Sdk createAldorUnitSdk() {
            Sdk theSdk = new ProjectJdkImpl("AldorUnit SDK", AldorUnitSdkType.instance());

            SdkModificator mod = theSdk.getSdkModificator();
            mod.setHomePath(aldorUnitHomePath);
            mod.commitChanges();
            ProjectJdkTable.getInstance().addJdk(theSdk);
            return theSdk;
        }
    }

    public static LightProjectDescriptor fricasSdkProjectDescriptor(String prefix) {
        return instance.getProjectDescriptor(SdkOption.Fricas, prefix);
    }

    public static LightProjectDescriptor aldorSdkProjectDescriptor(String prefix) {
        return instance.getProjectDescriptor(SdkOption.Aldor, prefix);
    }

    public static LightProjectDescriptor aldorSdkProjectDescriptorWithAldorUnit(String prefix) {
        return instance.getProjectDescriptor(new WithAldorUnit(SdkOption.Aldor), prefix);
    }

    public static LightProjectDescriptor fricasLocalSdkProjectDescriptor(String prefix) {
        return instance.getProjectDescriptor(SdkOption.FricasLocal, prefix);
    }

    public static LightProjectDescriptor aldorLocalSdkProjectDescriptor(String prefix) {
        return instance.getProjectDescriptor(SdkOption.AldorLocal, prefix);
    }

    private LightProjectDescriptor getProjectDescriptor(SdkDescriptor sdkDescriptor, String prefix) {
        return descriptorForPrefix.computeIfAbsent(sdkDescriptor.name(prefix), k -> new SdkLightProjectDescriptor(sdkDescriptor, prefix));
    }

    private static class SdkLightProjectDescriptor extends LightProjectDescriptor {
        private final String prefix;
        private final SdkDescriptor sdkDescriptor;
        private Sdk sdk = null;

        SdkLightProjectDescriptor(SdkDescriptor sdkDescriptor, String prefix) {
            this.sdkDescriptor = sdkDescriptor;
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
            ApplicationManagerEx.getApplicationEx().setSaveAllowed(true);
            super.setUpProject(project, handler);
            project.save();
        }

        @Override
        protected void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
            super.configureModule(module, model, contentEntry);
            System.out.println("Configuring module " + module + " " + model);
            switch (this.sdkDescriptor.sdkOption()) {
                case FricasLocal: {
                    configureLocalFricas(model);
                    break;
                }
                case AldorLocal: {
                    configureLocalAldor(model);
                    break;
                }
                case Aldor: {
                    configureInstalledAldor(model);
                    AldorModulePathService pathService = AldorModulePathService.getInstance(module);
                    pathService.getState().setOutputDirectory("out/ao");
                    pathService.getState().setMakeDirectory(AldorMakeDirectoryOption.Source);
                    break;
                }
                default:
            }
        }

        private void configureInstalledAldor(@NotNull ModifiableRootModel model) {
            CompilerModuleExtension compilerModuleExtension = model.getModuleExtension(CompilerModuleExtension.class);
            compilerModuleExtension.setCompilerOutputPath("file:///tmp/test_output");
            compilerModuleExtension.inheritCompilerOutputPath(false);

            Disposable disposable = new CompositeDisposable();
            WizardContext context = new WizardContext(model.getProject(), disposable);
            ProjectTemplate[] templates = new AldorTemplateFactory().createTemplates("Aldor", context);

            ProjectTemplate template = Arrays.stream(templates).filter(t -> "Simple Aldor module".equals(t.getName())).findFirst().orElse(null);
            Assert.assertNotNull(template);

            AldorSimpleModuleBuilder builder = (AldorSimpleModuleBuilder) template.createModuleBuilder();
            try {
                builder.setupRootModel(model);
            } catch (ConfigurationException e) {
                throw new RuntimeException("Failed to build module ", e);
            }
            disposable.dispose();
        }

        private void configureLocalAldor(@NotNull ModifiableRootModel model) {
            ContentEntry newContentEntry = model.addContentEntry("file://" + prefix);
            for (String sourceDir: AldorLocalSdkType.ALDOR_SOURCE_DIRS) {
                newContentEntry.addSourceFolder("file://" + prefix + "/aldor/" + sourceDir, false);
            }
            for (String testDir: AldorLocalSdkType.ALDOR_TEST_DIRS) {
                newContentEntry.addSourceFolder("file://" + prefix + "/aldor" + testDir, true);
            }

            CompilerModuleExtension moduleExtension = model.getModuleExtension(CompilerModuleExtension.class);
            moduleExtension.inheritCompilerOutputPath(false);
            moduleExtension.setCompilerOutputPath("file://" + prefix + "/build");
        }

        private void configureLocalFricas(@NotNull ModifiableRootModel model) {
            ContentEntry newContentEntry = model.addContentEntry("file://" + prefix);
            newContentEntry.addSourceFolder("file://" + prefix +"/fricas/src", false);
            CompilerModuleExtension moduleExtension = model.getModuleExtension(CompilerModuleExtension.class);
            moduleExtension.inheritCompilerOutputPath(false);
            moduleExtension.setCompilerOutputPath("file://" + prefix + "/build");
        }

        @Override
        protected VirtualFile createSourceRoot(@NotNull Module module, String srcPath) {
            try {
                VirtualFile root = module.getProject().getBaseDir().getFileSystem().findFileByPath("/tmp");
                assert root != null;
                String moduleName = module.getProject().getName() + "_" + module.getName();
                VirtualFile srcRoot = root.findChild(moduleName);
                if (srcRoot == null) {
                    return root.createChildDirectory(null, moduleName);
                }
                else {
                    srcRoot.refresh(false, false);
                    return srcRoot;
                }
            }catch (IOException e) {
                throw new RuntimeException("No way", e);
            }
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
            Sdk theSdk = new ProjectJdkImpl(sdkDescriptor.name(prefix), sdkDescriptor.sdkOption().sdkType());

            SdkModificator mod = theSdk.getSdkModificator();
            mod.setHomePath(prefix);
            mod.commitChanges();
            sdkDescriptor.sdkOption().sdkType().setupSdkPaths(theSdk);
            sdkDescriptor.editSdk(theSdk);
            return theSdk;
        }

    }
}
