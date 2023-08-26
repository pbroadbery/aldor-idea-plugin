package aldor.builder.maketarget;

import aldor.builder.AldorBuilderService;
import aldor.builder.jps.module.ConfigRootFacetProperties;
import aldor.builder.jps.module.JpsAldorFacetExtension;
import aldor.builder.jps.module.JpsConfiguredRootFacetExtension;
import com.intellij.openapi.diagnostic.LogLevel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.UsefulTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsNamedElement;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.impl.JpsEventDispatcherBase;
import org.jetbrains.jps.model.impl.JpsModelImpl;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleType;

import java.io.File;
import java.util.List;

public class MakeBuildTargetTest extends UsefulTestCase {
    private static final Logger LOG = Logger.getInstance(MakeBuildTargetTest.class);
    private File myDataStorageRoot;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Logger.setUnitTestMode();
        myDataStorageRoot = FileUtil.createTempDirectory("compile-server-" + getProjectName(), null);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            LOG.info("Finished test");
        }
        finally {
            super.tearDown();
        }
    }

    protected String getProjectName() {
        return StringUtil.decapitalize(StringUtil.trimStart(getName(), "test"));
    }

    public void testMakeTargets() {
        Logger.setUnitTestMode();
        LOG.setLevel(LogLevel.ALL);

        JpsModel model = new JpsModelImpl(new SimpleEventDispatcher());
        model.getProject().addModule(createRootModule());
        model.getProject().addModule(createAldorModule());

        AldorBuilderService builderService = new AldorBuilderService();

        List<? extends BuildTargetType<?>> targetTypes = builderService.getTargetTypes();
        // extract targets.  Check that buildAll, testAll and clean all exist
        /*
        Assert.assertTrue(targetTypes.contains(builderService.targetTypes().makeBuildTargetType));

        List<MakeBuildTarget> targets = builderService.targetTypes().makeBuildTargetType.computeAllTargets(model);
        Assert.assertTrue(targets.stream().map(x -> x.targetName()).anyMatch(x -> x.equals("all")));
        Assert.assertTrue(targets.stream().map(x -> x.targetName()).anyMatch(x -> x.equals("clean")));

        MakeBuildTarget all = targets.stream().filter(x -> x.targetName().equals("all")).findFirst().get();
        LOG.info("Targets: " + targets.stream().map(x -> x.targetName()).toList());
        LOG.info("All: " + all);
        LOG.info("TargetName: " + all.targetName());
        LOG.info("TargetDirectory: " + all.targetDirectory());
        Assert.assertEquals("/tmp/project/aldor", all.directory().getPath());
        Assert.assertEquals("../build", all.targetDirectory().getPath());
        Assert.assertEquals("all", all.targetName());

         */
    }

    public void testAutoconfTargets() {
        Logger.setUnitTestMode();
        LOG.setLevel(LogLevel.ALL);
        JpsModel model = new JpsModelImpl(new SimpleEventDispatcher());
        model.getProject().addModule(createRootModule());
        model.getProject().addModule(createAldorModule());

        AldorBuilderService builderService = new AldorBuilderService();
       /*
        LOG.info("Targets: " + targets);

        Assert.assertEquals(2, targets.size());
        AutoconfBuildTarget autogen = targets.stream().filter(x -> x.stage() == AutoconfBuildTarget.AutoconfStage.Autogen).findFirst().get();
        Assert.assertEquals("/tmp/project/aldor", autogen.directory().getPath());

        IgnoredFileIndex fileIndex = new IgnoredFileIndexImpl(model);

        BuildDataPaths dataPaths = new BuildDataPathsImpl(myDataStorageRoot);
        List<MakeTargetRootDescriptor> descriptors = autogen.computeRootDescriptors(model, new ModuleExcludeIndexImpl(model), fileIndex, dataPaths);
        assertEmpty(descriptors);

        */
    }

    private JpsModule createAldorModule() {
        JpsModule rootModule = JpsElementFactory.getInstance().createModule("libDir", new JpsEmptyModuleType(), JpsElementFactory.getInstance().createDummyElement());
        JpsAldorFacetExtension facetExtension = new JpsAldorFacetExtension();
        facetExtension.install(rootModule);
        return rootModule;
    }

    private JpsModule createRootModule() {
        JpsModule rootModule = JpsElementFactory.getInstance().createModule("root", new JpsEmptyModuleType(), JpsElementFactory.getInstance().createDummyElement());
        ConfigRootFacetProperties properties = ConfigRootFacetProperties.newBuilder().setDefined(true).setBuildDirectory("../build").build();
        JpsConfiguredRootFacetExtension rootFacetExtension = new JpsConfiguredRootFacetExtension(properties);

        rootFacetExtension.install(rootModule);
        return rootModule;
    }

    private static class JpsEmptyModuleType extends JpsElementTypeBase<JpsElement> implements JpsModuleType<JpsElement> {

    }

    private static class SimpleEventDispatcher extends JpsEventDispatcherBase {
        @Override
        public void fireElementRenamed(@NotNull JpsNamedElement element, @NotNull String oldName, @NotNull String newName) {
            LOG.info("elementRenamed " + element.getName() + " "+ oldName + " " + newName);
        }

        @Override
        public void fireElementChanged(@NotNull JpsElement element) {
            LOG.info("elementChanged " + element);
        }
    }

}