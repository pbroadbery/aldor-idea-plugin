package aldor.builder.files;

import aldor.builder.AldorBuilderService;
import aldor.builder.jps.AldorSourceRootProperties;
import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.JpsAldorModelSerializerExtension;
import aldor.builder.jps.module.AldorFacetExtensionProperties;
import aldor.builder.jps.module.AldorModuleFacade;
import aldor.builder.jps.module.JpsAldorFacetExtension;
import aldor.builder.jps.module.JpsAldorModuleType;
import aldor.util.JUnitsRt;
import com.intellij.openapi.util.io.FileUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsEventDispatcher;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsNamedElement;
import org.jetbrains.jps.model.impl.JpsEventDispatcherBase;
import org.jetbrains.jps.model.impl.JpsModelImpl;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AldorFileBuildTargetTypeTest {
    private File rootDirectory = null;
    private JpsModel model = null;
    @Rule
    public final TestRule rules = RuleChain.emptyRuleChain()
        .around(JUnitsRt.setLogToDebugTestRule);

    @Before
    public void setUp() throws IOException {
        model = emptyModel();
        rootDirectory = Files.createTempDirectory("silly-module").toFile();
    }

    @After
    public void tearDown() {
        if (!FileUtilRt.delete(rootDirectory)) {
            throw new RuntimeException("Failed to remove temporary directory " + rootDirectory);
        }
    }

    @Test
    public void testSdk() {
        initModule("mod-1");
        addAldorSourceRoot("mod-1", "src");
        addSdk("mod-1", "aldor-sdk", "/tmp/aldor-sdk");

        AldorModuleFacade facade = new AldorModuleFacade(model.getProject().getModules().get(0));
        //noinspection deprecation
        assertEquals("aldor-sdk", facade.facet().sdkName());
        assertEquals("/tmp/aldor-sdk", facade.sdkPath());
    }

    @Test
    public void testTargets() {
        initModule("mod-1");
        addAldorSourceRoot("mod-1", "src");
        addSdk("mod-1", "aldor-sdk", "/tmp/aldor-sdk");

        AldorBuilderService builderService = new AldorBuilderService();
        AldorFileBuildTargetType targetType = new AldorFileBuildTargetType(builderService);
        List<AldorFileBuildTarget> targets = targetType.computeAllTargets(model);
        assertTrue(targets.isEmpty());
    }

    @Test
    public void testOneFile() {
        initModule("mod-1");
        addAldorSourceRoot("mod-1", "src");
        addSdk("mod-1", "aldor-sdk", "/tmp/aldor-sdk");

        createFile("mod-1", "src", "Makefile", "a makefile");
        createFile("mod-1", "src", "base/foo.as", "never");

        AldorBuilderService builderService = new AldorBuilderService();
        AldorFileBuildTargetType targetType = new AldorFileBuildTargetType(builderService);
        List<AldorFileBuildTarget> targets = targetType.computeAllTargets(model);
        assertEquals(1, targets.size());

        AldorFileBuildTarget target = targets.get(0);
        assertEquals(Path.of(rootDirectory.getPath(), "mod-1", "src").toFile(), target.buildDirectory());
        assertEquals("base/foo.ao", target.makeTargetName());
    }

    void createFile(String module, String sourceRoot, String path, String text) {
        Path file = Path.of(rootDirectory.getPath(), module, sourceRoot, path);
        try {
            if (!file.toFile().getParentFile().mkdirs()) {
                throw new IllegalStateException("Failed to create directory for " + file.toAbsolutePath());
            }
            Files.write(file.toAbsolutePath(), Collections.singletonList(text));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialises module with contentRoot = base/moduleName
     * @param moduleName
     * @throws IOException
     */
    private void initModule(String moduleName) {
        JpsModel modifiable = model.createModifiableModel(dispatcher(moduleName));
        try {
            JpsModule module = modifiable.getProject().addModule(moduleName, JpsAldorModuleType.INSTANCE);
            File moduleRoot = new File(rootDirectory, moduleName);
            module.getContentRootsList().addUrl("file://" + moduleRoot.getAbsolutePath());
        }
        finally {
            modifiable.commit();
        }
    }

    void addAldorSourceRoot(String moduleName, String path) {
        addAldorSourceRoot(moduleName, path, AldorSourceRootType.INSTANCE.createDefaultProperties());
    }

    void addAldorSourceRoot(String moduleName, String path, @NotNull AldorSourceRootProperties properties) {
        JpsModel modifiable = model.createModifiableModel(dispatcher("source-" + path));
        JpsModule module = modifiable.getProject().getModules().stream().filter(mod -> mod.getName().equals(moduleName)).findFirst().orElseThrow();
        JpsModuleSourceRoot sourceRoot = JpsElementFactory.getInstance().createModuleSourceRoot("file:///" + rootDirectory + "/" + moduleName + "/" + path,
                AldorSourceRootType.INSTANCE, properties);
        module.addSourceRoot(sourceRoot);
        modifiable.commit();
    }

    void addSdk(String moduleName, String sdkName, String sdkPath) {
        JpsModel modifiable = model.createModifiableModel(dispatcher("sdk-" + sdkPath));
        JpsModule module = modifiable.getProject().getModules().stream().filter(mod -> mod.getName().equals(moduleName)).findFirst().orElseThrow();

        AldorFacetExtensionProperties properties = facetProperties(module);
        properties = properties.asBuilder().setSdkName(sdkName).build();
        setFacetProperties(module, properties);

        JpsAldorModelSerializerExtension.JpsAldorSdkType sdkType = JpsAldorModelSerializerExtension.JpsAldorSdkType.INSTALLED;
        modifiable.getProject().getSdkReferencesTable().setSdkReference(sdkType,
                JpsElementFactory.getInstance().createSdkReference(sdkName, sdkType));
        modifiable.getGlobal().addSdk(sdkName, sdkPath, "1.0.0", sdkType, sdkType.createDefaultProperties());
        modifiable.commit();
    }

    private void setFacetProperties(JpsModule module, AldorFacetExtensionProperties properties) {
        module.getContainer().setChild(JpsAldorFacetExtension.ROLE, new JpsAldorFacetExtension(properties));
    }

    private AldorFacetExtensionProperties facetProperties(JpsModule module) {
        return Optional.ofNullable(module.getContainer().getChild(JpsAldorFacetExtension.ROLE))
                .map(JpsAldorFacetExtension::getProperties)
                .orElse(AldorFacetExtensionProperties.builder().build());
    }

    @NotNull
    private JpsModel emptyModel() {
        JpsEventDispatcher dispatcher = dispatcher("model");
        return new JpsModelImpl(dispatcher);
    }

    @NotNull
    private JpsEventDispatcher dispatcher(String name) {
        return new JpsEventDispatcherBase() {
                @Override
                public void fireElementRenamed(@NotNull JpsNamedElement element, @NotNull String oldName, @NotNull String newName) {
                    System.out.println("Rename: (" + name + ")" + oldName + " --> " + newName);
                }

                @Override
                public void fireElementChanged(@NotNull JpsElement element) {
                    System.out.println("Changed: (" + name + ")" + element);
                }
            };
    }

}