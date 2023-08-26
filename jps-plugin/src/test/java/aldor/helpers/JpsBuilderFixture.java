package aldor.helpers;

import aldor.builder.maketarget.TestProjectBuilderLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.api.CanceledStatus;
import org.jetbrains.jps.builders.impl.BuildDataPathsImpl;
import org.jetbrains.jps.builders.impl.BuildRootIndexImpl;
import org.jetbrains.jps.builders.impl.BuildTargetIndexImpl;
import org.jetbrains.jps.builders.impl.BuildTargetRegistryImpl;
import org.jetbrains.jps.builders.logging.BuildLoggingManager;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.cmdline.BuildRunner;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
import org.jetbrains.jps.incremental.BuilderRegistry;
import org.jetbrains.jps.incremental.CompileScope;
import org.jetbrains.jps.incremental.IncProjectBuilder;
import org.jetbrains.jps.incremental.RebuildRequestedException;
import org.jetbrains.jps.incremental.fs.BuildFSState;
import org.jetbrains.jps.incremental.relativizer.PathRelativizerService;
import org.jetbrains.jps.incremental.storage.BuildDataManager;
import org.jetbrains.jps.incremental.storage.BuildTargetsState;
import org.jetbrains.jps.incremental.storage.ProjectStamps;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.indices.impl.IgnoredFileIndexImpl;
import org.jetbrains.jps.indices.impl.ModuleExcludeIndexImpl;
import org.jetbrains.jps.model.JpsModel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;

public class JpsBuilderFixture {
    private static final Logger LOG = Logger.getInstance(JpsBuilderFixture.class);
    private final String projectName;
    private JpsModel myModel;
    private File myDataStorageRoot;
    private aldor.builder.maketarget.TestProjectBuilderLogger myLogger;

    public JpsBuilderFixture(String projectName) throws IOException {
        this.projectName = projectName;
        this.myModel = null;
        this.myDataStorageRoot = null;
        this.myLogger = null;
    }

    public void setUp() throws IOException {
        myDataStorageRoot = FileUtil.createTempDirectory("compile-server-" + projectName, null);
        myLogger = new TestProjectBuilderLogger();
    }

    public void setModel(JpsModel model) {
        this.myModel = model;
    }

    public JpsModel model() {
        return myModel;
    }

    public ProjectDescriptor createProjectDescriptor() {
        return createProjectDescriptor(new BuildLoggingManager(myLogger));
    }

    private ProjectDescriptor createProjectDescriptor(BuildLoggingManager buildLoggingManager) {
        try {
            BuildTargetRegistryImpl targetRegistry = new BuildTargetRegistryImpl(myModel);
            ModuleExcludeIndex index = new ModuleExcludeIndexImpl(myModel);
            IgnoredFileIndexImpl ignoredFileIndex = new IgnoredFileIndexImpl(myModel);
            BuildDataPaths dataPaths = new BuildDataPathsImpl(myDataStorageRoot);
            BuildRootIndexImpl buildRootIndex = new BuildRootIndexImpl(targetRegistry, myModel, index, dataPaths, ignoredFileIndex);
            BuildTargetIndexImpl targetIndex = new BuildTargetIndexImpl(targetRegistry, buildRootIndex);
            BuildTargetsState targetsState = new BuildTargetsState(dataPaths, myModel, buildRootIndex);
            PathRelativizerService relativizerService = new PathRelativizerService(myModel.getProject());
            ProjectStamps timestamps = new ProjectStamps(myDataStorageRoot, targetsState, relativizerService);
            BuildDataManager dataManager = new BuildDataManager(dataPaths, targetsState, relativizerService);
            return new ProjectDescriptor(myModel, new BuildFSState(true), timestamps, dataManager, buildLoggingManager, index,
                    targetIndex, buildRootIndex, ignoredFileIndex);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            //preloadedDataExtension.discardPreloadedData(preloadedData);
        }
    }

    public CompileScope buildScopeFromPairs(ProjectDescriptor descriptor, List<TargetTypeBuildScope> pairs) throws Exception {
        BuildRunner runner = new BuildRunner(() -> myModel);
        return runner.createCompilationScope(descriptor, pairs);
    }

    public BuildResult doBuild(final ProjectDescriptor descriptor, CompileScopeTestBuilder scopeBuilder) {
        CompileScope scope = scopeBuilder.build();
        return doBuildFromScope(descriptor, scope);
    }

    @NotNull
    public BuildResult doBuildFromScope(ProjectDescriptor descriptor, CompileScope scope) {
        IncProjectBuilder builder = new IncProjectBuilder(descriptor, BuilderRegistry.getInstance(), new HashMap<>(), CanceledStatus.NULL, true);
        BuildResult result = new BuildResult();
        builder.addMessageHandler(result);
        try {
            beforeBuildStarted(descriptor);
            builder.build(scope, false);
            result.storeMappingsDump(descriptor);
        }
        catch (RebuildRequestedException | IOException e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Called as a build is starting
     * @param descriptor The project
     * TODO: Listener?
     */
    protected void beforeBuildStarted(@NotNull ProjectDescriptor descriptor) {
        LOG.info("Staring build");
    }

    protected BuildResult rebuildAllAndSucceed() {
        BuildResult res = doBuild(CompileScopeTestBuilder.rebuild().all());
        res.assertSuccessful();
        return res;
    }

    protected BuildResult rebuildAllAndFail() {
        BuildResult res = doBuild(CompileScopeTestBuilder.rebuild().all());
        res.assertFailed();
        return res;
    }

    protected BuildResult makeAll() {
        return doBuild(CompileScopeTestBuilder.make().all());
    }

    public BuildResult doBuild(CompileScopeTestBuilder scope) {
        ProjectDescriptor descriptor = createProjectDescriptor(new BuildLoggingManager(myLogger));
        try {
            myLogger.clearFilesData();
            return doBuild(descriptor, scope);
        }
        finally {
            descriptor.release();
        }
    }


    public void tearDown() {

    }

}
