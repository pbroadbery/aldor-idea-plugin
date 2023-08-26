package aldor.builder.maketarget;

import aldor.builder.jps.AldorSourceRootProperties;
import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.module.AldorFacetProperties;
import aldor.builder.jps.module.AldorModuleState;
import aldor.builder.jps.module.ConfigRootFacetProperties;
import aldor.builder.jps.module.JpsAldorFacetExtension;
import aldor.builder.jps.module.JpsAldorModuleType;
import aldor.builder.jps.module.JpsConfiguredRootFacetExtension;
import aldor.builder.jps.module.MakeConvention;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsNamedElement;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.impl.JpsEventDispatcherBase;
import org.jetbrains.jps.model.impl.JpsModelImpl;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.model.module.JpsModuleType;
import org.jetbrains.jps.model.serialization.impl.JpsProjectSerializationDataExtensionImpl;

import java.io.File;
import java.io.IOException;

public class GitRepositoryFixture {
    private static final Logger LOG = Logger.getInstance(GitRepositoryFixture.class);
    private static final String ALDOR_REPO_LOC = "/home/pab/Work/aldorgit/utypes/aldor";
    private final File rootDirectory;
    private JpsModel model = null;
    private boolean isCloned = false;

    public GitRepositoryFixture(File myRootDirectory) {
        this.rootDirectory = myRootDirectory;
    }

    public boolean canCreate() {
        return new File(ALDOR_REPO_LOC).isDirectory();
    }

    public void cloneDirectory() {
        if (!rootDirectory().mkdirs() && !rootDirectory.exists()) {
            throw new RuntimeException("Failed to create " + rootDirectory());
        }
        var builder = new ProcessBuilder()
                .directory(rootDirectory())
                .command("git", "clone", ALDOR_REPO_LOC);
        LOG.info("Starting " + builder.command());
        LOG.info("... in " + builder.directory());
        try {
            var process = builder.start();
            int result = process.waitFor();
            if (result != 0) {
                throw new RuntimeException("Failed to clone project: " + result);
            }
            isCloned = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted! ", e);
        }
    }

    public JpsModel getJpsModel() {
        if (this.model == null) {
            JpsModel jpsModel = new JpsModelImpl(new SimpleEventDispatcher());
            jpsModel.getProject().addModule(createRootModule());
            jpsModel.getProject().addModule(createAldorModule());
            jpsModel.getProject().getContainer().setChild(JpsProjectSerializationDataExtensionImpl.ROLE,
                    new JpsProjectSerializationDataExtensionImpl(rootDirectory.toPath()));
            this.model = jpsModel;
        }
        return model;
    }

    // Just create a module for the 'aldor' (core types) module - algebra, ... are not needed for testing the
    // build (or can be added later)
    private JpsModule createAldorModule() {
        AldorModuleState state = AldorModuleState.newBuilder().build();
        JpsSimpleElement<AldorModuleState> simpleElement = JpsElementFactory.getInstance().createSimpleElement(state);
        JpsModule aldorModule = JpsElementFactory.getInstance().createModule("aldorlib", new JpsAldorModuleType(), simpleElement);
        JpsAldorFacetExtension facetExtension = new JpsAldorFacetExtension(AldorFacetProperties.newBuilder()
                .makeConvention(MakeConvention.Configured)
                .build());
        facetExtension.install(aldorModule);
        @NotNull AldorSourceRootProperties properties = new AldorSourceRootProperties("");
        JpsModuleSourceRoot sourceRoot = JpsElementFactory.getInstance().createModuleSourceRoot("file:///" + rootDirectory + "/aldor/aldor/lib/aldor/src",
                AldorSourceRootType.INSTANCE, properties);
        aldorModule.addSourceRoot(sourceRoot);
        return aldorModule;
    }

    private JpsModule createRootModule() {
        JpsModule rootModule = JpsElementFactory.getInstance().createModule("root", new JpsEmptyModuleType(), JpsElementFactory.getInstance().createDummyElement());
        ConfigRootFacetProperties properties = ConfigRootFacetProperties.newBuilder().setDefined(true).setBuildDirectory("../../build").build();
        JpsConfiguredRootFacetExtension rootFacetExtension = new JpsConfiguredRootFacetExtension(properties);

        rootFacetExtension.install(rootModule);
        //rootModule.addSourceRoot(subDirectoryAsURL(rootDirectory(), "aldor/aldor"), ConfigSourceRootType.INSTANCE);
        rootModule.getContentRootsList().addUrl(subDirectoryAsURL(rootDirectory, "aldor/aldor"));
        return rootModule;
    }

    String subDirectoryAsURL(File root, String subDir) {
        return "file://" + root.getAbsoluteFile() + "/" + subDir;
    }

    public File rootDirectory() {
        return rootDirectory;
    }

    private static class JpsEmptyModuleType extends JpsElementTypeBase<JpsElement> implements JpsModuleType<JpsElement> {

    }

    private static class SimpleEventDispatcher extends JpsEventDispatcherBase {
        @Override
        public void fireElementRenamed(@NotNull JpsNamedElement element, @NotNull String oldName, @NotNull String newName) {
            LOG.info("elementRenamed " + element.getName() + " " + oldName + " " + newName);
        }

        @Override
        public void fireElementChanged(@NotNull JpsElement element) {
            LOG.info("elementChanged " + element);
        }
    }


}
