package aldor.test_util;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.fricas.FricasFacet;
import aldor.build.facet.fricas.FricasFacetProperties;
import aldor.build.module.AldorModuleType;
import aldor.builder.jps.AldorModuleExtensionProperties;
import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.JpsAldorMakeDirectoryOption;
import aldor.module.template.AldorSimpleModuleBuilder;
import aldor.module.template.AldorTemplateFactory;
import aldor.sdk.aldor.AldorInstalledSdkType;
import aldor.sdk.aldor.AldorLocalSdkType;
import aldor.sdk.fricas.FricasInstalledSdkType;
import aldor.sdk.fricas.FricasLocalSdkType;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.CompositeDisposable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectTemplate;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assume;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertNotNull;

public final class SdkProjectDescriptors {
    private static final Logger LOG = Logger.getInstance(SdkProjectDescriptors.class);
    private static final SdkProjectDescriptors instance = new SdkProjectDescriptors();
    private final Map<String, LightProjectDescriptor> descriptorForPrefix;

    public SdkProjectDescriptors() {
        descriptorForPrefix = new ConcurrentHashMap<>();
    }

    private interface SdkDescriptor {
        @SuppressWarnings("ClassReferencesSubclass")
        SdkOption sdkOption();
        String name(String prefix);

        Sdk editSdk(Sdk theSdk);

        ModuleType getModuleType();

        void editFacet(Module module);
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

        @Override
        public ModuleType getModuleType() {
            return AldorModuleType.instance();
        }

        @Override
        public void editFacet(Module module) {
        }
    }

    private static class WithAldorUnit implements SdkDescriptor {
        private SdkDescriptor innerSdkDescriptor;
        private String aldorUnitHomePath = "/home/pab/IdeaProjects/type-library/out/artifacts/aldorunit";

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
            return theSdk;
        }

        @Override
        public ModuleType getModuleType() {
            return innerSdkDescriptor.getModuleType();
        }

        @Override
        public void editFacet(Module module) {
            Sdk javaSdk = createJDK();
            AldorFacet facet = AldorFacet.forModule(module);
            facet.getConfiguration().loadState(facet.getConfiguration().getState().asBuilder()
                    .setOutputDirectory("out/ao")
                    .setOption(JpsAldorMakeDirectoryOption.Source)
                    .setBuildJavaComponents(true)
                    .setJavaSdkName(javaSdk.getName())
                    .build());
        }

        private Sdk createJDK() {
            Sdk jdk = JavaSdk.getInstance().createJdk("aldorunit-java", System.getProperty("java.home"));
            ProjectJdkTable.getInstance().addJdk(jdk);
            return jdk;
        }
    }

    public static LightProjectDescriptor fricasSdkProjectDescriptor(DirectoryPresentRule prefix) {
        Assume.assumeTrue(prefix.isPresent());
        return instance.getProjectDescriptor(SdkOption.Fricas, prefix.path());
    }

    public static LightProjectDescriptor fricasSdkProjectDescriptor(ExecutablePresentRule rule) {
        Assume.assumeTrue(rule.shouldRunTest());
        return instance.getProjectDescriptor(SdkOption.Fricas, rule.prefix());
    }

    public static LightProjectDescriptor aldorSdkProjectDescriptor(String prefix) {
        return instance.getProjectDescriptor(SdkOption.Aldor, prefix);
    }

    public static LightProjectDescriptor aldorSdkProjectDescriptor(ExecutablePresentRule rule) {
        Assume.assumeTrue(rule.shouldRunTest());
        Assume.assumeTrue(false);
        return instance.getProjectDescriptor(SdkOption.Aldor, rule.prefix());
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
        //private Sdk sdk = null;

        SdkLightProjectDescriptor(SdkDescriptor sdkDescriptor, String prefix) {
            this.sdkDescriptor = sdkDescriptor;
            this.prefix = prefix;
        }

        @NotNull
        @Override
        public String getModuleTypeId() {
            return sdkDescriptor.getModuleType().getId();
        }

        @Override
        public void setUpProject(@NotNull Project project, @NotNull SetupHandler handler) throws Exception {
            //WriteAction.run( () -> ProjectRootManager.getInstance(project).setProjectSdk(sdk()));
            //ApplicationManagerEx.getApplicationEx().setSaveAllowed(true);
            super.setUpProject(project, handler);
            //project.save();
        }

        @Nullable
        private ModuleBuilder builder = null;
        @Nullable
        private Disposable disposable = null;

        @Override
        protected void createContentEntry(@NotNull Module module, @NotNull VirtualFile srcRoot) {
            disposable = new CompositeDisposable();
            builder = createBuilder(module, srcRoot);
            super.createContentEntry(module, srcRoot);
            createFacet(module);
            disposable.dispose();
        }

        private void createFacet(@NotNull Module module) {
            if (sdkDescriptor.sdkOption() == SdkOption.Aldor) {
                createAldorFacet(module);
                sdkDescriptor.editFacet(module);
            }
            if (sdkDescriptor.sdkOption() == SdkOption.Fricas) {
                createFricasFacet(module);
                sdkDescriptor.editFacet(module);
            }
        }

        private @Nullable ModuleBuilder createBuilder(@NotNull Module module, @NotNull VirtualFile srcRoot) {
            if (sdkDescriptor.sdkOption() == SdkOption.Aldor) {
                return createAldorBuilder(module, srcRoot);
            }
            return null;
        }

        private AldorSimpleModuleBuilder createAldorBuilder(Module module, VirtualFile srcRoot) {
            WizardContext context = new WizardContext(module.getProject(), disposable);
            ProjectTemplate[] templates = new AldorTemplateFactory().createTemplates("Aldor", context);

            ProjectTemplate template = Arrays.stream(templates).filter(t -> "Simple Aldor module".equals(t.getName())).findFirst().orElse(null);
            assertNotNull(template);
            AldorSimpleModuleBuilder aldorBuilder = (AldorSimpleModuleBuilder) template.createModuleBuilder();
            aldorBuilder.setCreateInitialStructure(false);
            aldorBuilder.setSdk(Objects.requireNonNull(this.getSdk()));
            aldorBuilder.setContentEntryPath(srcRoot.getPath());
            return aldorBuilder;
        }

        private void createAldorFacet(Module module) {
            assertNotNull(builder);
            AldorModuleExtensionProperties properties = ((AldorSimpleModuleBuilder) builder).properties();
            AldorFacet.createFacetIfMissing(module, properties);
        }

        private void createFricasFacet(Module module) {
            FricasFacetProperties properties = new FricasFacetProperties(getSdk().getName());
            FricasFacet.createFacetIfMissing(module, properties);
        }

        @Override
        protected void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
            super.configureModule(module, model, contentEntry);

            switch (this.sdkDescriptor.sdkOption()) {
                case FricasLocal:
                    configureLocalFricas(model);
                    break;
                case Fricas:
                    break;
                case AldorLocal:
                    configureLocalAldor(model);
                    break;
                case Aldor:
                    configureInstalledAldor(model, module, contentEntry);
                    break;
                default:
            }
        }

        private void configureInstalledAldor(@NotNull ModifiableRootModel model, Module module, ContentEntry contentEntry) {
            LOG.info("Configuring aldor - module " + model.getModule().getName() + " " + contentEntry.getUrl());
            //CompilerModuleExtension compilerModuleExtension = model.getModuleExtension(CompilerModuleExtension.class);
            //compilerModuleExtension.setCompilerOutputPath("file:///tmp/test_output");
            //compilerModuleExtension.inheritCompilerOutputPath(false);

            model.addContentEntry(contentEntry.getUrl());
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
                newContentEntry.addSourceFolder("file://" + prefix + "/aldor/" + sourceDir, AldorSourceRootType.INSTANCE);
            }
            for (String testDir: AldorLocalSdkType.ALDOR_TEST_DIRS) {
                newContentEntry.addSourceFolder("file://" + prefix + "/aldor" + testDir, AldorSourceRootType.INSTANCE);
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
            @Nullable Sdk sdk = ProjectJdkTable.getInstance().findJdk(sdkDescriptor.name(prefix));
            if (sdk == null) {
                sdk = createSDK();
                //ProjectJdkTable.getInstance().addJdk(sdk);
            }
            return sdk;
        }

        Sdk createSDK() {
            Sdk theSdk = new ProjectJdkImpl(sdkDescriptor.name(prefix), sdkDescriptor.sdkOption().sdkType());
            LOG.info("CreateSDK " + sdkDescriptor.sdkOption().sdkType() + " homePath " + prefix);
            SdkModificator mod = theSdk.getSdkModificator();
            mod.setHomePath(prefix);
            mod.commitChanges();
            sdkDescriptor.sdkOption().sdkType().setupSdkPaths(theSdk);
            sdkDescriptor.editSdk(theSdk);
            return theSdk;
        }

    }
}
