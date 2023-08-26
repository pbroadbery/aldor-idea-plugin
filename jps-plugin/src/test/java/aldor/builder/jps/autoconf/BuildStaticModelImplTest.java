package aldor.builder.jps.autoconf;

import aldor.builder.jps.autoconf.descriptors.BuildInstanceModel;
import aldor.builder.jps.autoconf.descriptors.BuildStaticModelImpl;
import aldor.builder.maketarget.GitRepositoryFixture;
import aldor.helpers.CompileScopeTestBuilder;
import aldor.helpers.JpsBuilderFixture;
import aldor.util.AssumptionAware;
import aldor.util.Classes;
import aldor.util.HasSxForm;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import junit.framework.AssertionFailedError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.api.CmdlineRemoteProto;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
import org.jetbrains.jps.incremental.CompileScope;
import org.junit.Assert;
import org.junit.Assume;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope.newBuilder;

public class BuildStaticModelImplTest extends AssumptionAware.UsefulTestCase {
    private static final Logger LOG = Logger.getInstance(BuildStaticModelImplTest.class);
    private JpsBuilderFixture builderFixture;
    private GitRepositoryFixture gitCloneFixture;

    public BuildStaticModelImplTest() {
        Logger.setUnitTestMode();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        var mySourceDirectory = FileUtil.createTempDirectory("compile-server-" + getProjectName(), null);

        gitCloneFixture = new GitRepositoryFixture(mySourceDirectory);
        Assume.assumeTrue(gitCloneFixture.canCreate());
        builderFixture = new JpsBuilderFixture(getProjectName());
        builderFixture.setUp();
    }

    public void testCreateModels() {
        StaticExecutionEnvironment executionEnvironment = new StaticExecutionEnvironment();
        BuildStaticModelImpl staticModel = new BuildStaticModelImpl(executionEnvironment);

        gitCloneFixture.cloneDirectory();
        var model = gitCloneFixture.getJpsModel();
        List<BuildInstanceModel> models = staticModel.computeModels(model);
        Assert.assertEquals(1, models.size());
        BuildInstanceModel instanceModel = models.get(0);

        Assert.assertEquals(new File(gitCloneFixture.rootDirectory(), "aldor/aldor"), instanceModel.rootDirectory());
        Assert.assertEquals(new File(gitCloneFixture.rootDirectory(), "build"), instanceModel.targetDirectory());

        Assert.assertTrue(instanceModel.matchesJpsModel(model));
    }

    public void testTargets() {
        StaticExecutionEnvironment executionEnvironment = new StaticExecutionEnvironment();

        //.targetTypes(staticModel.targetTypes().toArray(n -> new BuildTargetType[n]));

        gitCloneFixture.cloneDirectory();

        var model = gitCloneFixture.getJpsModel();
        builderFixture.setModel(model);
        ProjectDescriptor descriptor = builderFixture.createProjectDescriptor();

        ScriptBuildTargetType type = ScriptBuildTargetType.ident.findType();
        List<ScriptBuildTarget> allTargets = descriptor.getBuildTargetIndex().getAllTargets(type);

        var salPartialTgt = allTargets.stream().filter(x -> x.getId().contains("sal_partial")).findFirst().orElse(null);
        Assert.assertNotNull(salPartialTgt);
        assertEquals("{root}-{lib/aldor/src/base}-{sal_partial.abn}", salPartialTgt.getId());

        var prereqSubcmds = allTargets.stream().filter(tgt -> tgt.getTargetType().equals(ScriptBuildTargetType.ident.findType()))
                .filter(tgt -> tgt.getId().contains("prereq")).toList();
        Assert.assertFalse(prereqSubcmds.isEmpty());
    }

    public void testModelSort() {

        gitCloneFixture.cloneDirectory();

        var model = gitCloneFixture.getJpsModel();
        builderFixture.setModel(model);
        ProjectDescriptor descriptor = builderFixture.createProjectDescriptor();

        TargetOutputIndex dummyIndex = new TargetOutputIndex() {
            @Override
            public Collection<BuildTarget<?>> getTargetsByOutputFile(@NotNull File file) {
                return Collections.emptyList();
            }
        };
        var allTgts = descriptor.getBuildTargetIndex().getAllTargets();
        for (var tgt: allTgts) {
            var deps = tgt.computeDependencies(descriptor.getBuildTargetIndex(), dummyIndex);
            Assert.assertTrue(allTgts.containsAll(deps));
        }

        var buildOne = allTgts.stream().filter(tgt -> tgt.getTargetType().equals(ScriptBuildTargetType.ident.findType()))
                .filter(tgt -> tgt.getId().contains("sal_partial")).findFirst().orElseThrow(() -> new AssertionFailedError());
        var deps = buildOne.computeDependencies(descriptor.getBuildTargetIndex(), dummyIndex);
        LOG.info("Deps: " + buildOne.getId() + " depends on --> " + deps);
        Assert.assertFalse(deps.isEmpty());

        var buildRuntime = allTgts.stream().filter(tgt -> tgt.getTargetType().equals(PhonyTargets.ident.findType()))
                .filter(tgt -> tgt.getId().contains("Runtime")).findFirst().orElseThrow(() -> new AssertionFailedError());
        var rdeps = buildRuntime.computeDependencies(descriptor.getBuildTargetIndex(), dummyIndex);
        LOG.info("RDeps: " + buildRuntime.getId() + " depends on --> " + rdeps);
        Assert.assertFalse(rdeps.isEmpty());

        var buildSubcmd = allTgts.stream().filter(tgt -> tgt.getTargetType().equals(ScriptBuildTargetType.ident.findType()))
                .filter(tgt -> tgt.getId().contains("subcmd")).findFirst().orElseThrow(() -> new AssertionFailedError());
        var subdeps = buildSubcmd.computeDependencies(descriptor.getBuildTargetIndex(), dummyIndex);
        LOG.info("SubDeps: " + buildSubcmd.getId() + " depends on --> " + subdeps.stream().map(x -> x.getId() + "(" + x.getPresentableName() + ")").toList());
        Assert.assertFalse(subdeps.isEmpty());

    }

    public void testRuntimeBuild() {
        gitCloneFixture.cloneDirectory();

        var model = gitCloneFixture.getJpsModel();
        builderFixture.setModel(model);
        ProjectDescriptor descriptor = builderFixture.createProjectDescriptor();
        var phonyTT = PhonyTargets.ident.findType();
        var tgt = descriptor.getBuildTargetIndex().getAllTargets(phonyTT)
                .stream()
                .filter(x -> "AldorRuntime".equals(x.descriptor().id()))
                .findAny().orElse(null);
        Assert.assertNotNull(tgt);
        CompileScopeTestBuilder scope = CompileScopeTestBuilder.make()
                .targetTypes(ScriptBuildTargetType.ident.findType(), PhonyTargets.ident.findType())
                .target(tgt);
        var result = builderFixture.doBuild(descriptor, scope);

        result.assertSuccessful();
        File configStatus = new File(gitCloneFixture.rootDirectory(), "build/config.status");
        Assert.assertTrue(configStatus.exists());
        Assert.assertTrue(result.isSuccessful());
    }


    public void testSalPartialScope() throws Exception {
        gitCloneFixture.cloneDirectory();

        var model = gitCloneFixture.getJpsModel();
        builderFixture.setModel(model);
        ProjectDescriptor descriptor = builderFixture.createProjectDescriptor();
        var phonyTT = PhonyTargets.ident.findType();
        var tgt = descriptor.getBuildTargetIndex().getAllTargets(phonyTT)
                .stream()
                .filter(x -> x.descriptor().id().contains("sal_partial"))
                .findAny().orElse(null);
        Assert.assertNotNull(tgt);
        List<CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope> pairs = new ArrayList<>();
        pairs.add(newBuilder().setTypeId(PhonyTargets.PhonyTargetType.ID).addTargetId(tgt.getId()).setForceBuild(true).build());
        CompileScope scope = builderFixture.buildScopeFromPairs(descriptor, pairs);
        Assert.assertTrue(scope.isAffected(tgt));
    }

    public void testTwoBuilds() throws Exception {
        gitCloneFixture.cloneDirectory();

        var model = gitCloneFixture.getJpsModel();
        builderFixture.setModel(model);
        ProjectDescriptor descriptor = builderFixture.createProjectDescriptor();
        var phonyTT = PhonyTargets.ident.findType();
        var tgt = descriptor.getBuildTargetIndex().getAllTargets(phonyTT)
                .stream()
                .filter(x -> x.descriptor().id().contains("just-subcmd"))
                .findAny().orElse(null);
        Assert.assertNotNull(tgt);
        List<CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope> pairs = new ArrayList<>();
        pairs.add(newBuilder().setTypeId(PhonyTargets.PhonyTargetType.ID).addTargetId(tgt.getId()).setForceBuild(true).build());
        pairs.add(newBuilder().setTypeId(ScriptBuildTargetType.ID).setAllTargets(true).setForceBuild(false).build());
        CompileScope scope = builderFixture.buildScopeFromPairs(descriptor, pairs);
        Assert.assertTrue(scope.isAffected(tgt));

        var result = builderFixture.doBuildFromScope(descriptor, scope);

        result.assertSuccessful();
        File configStatus = new File(gitCloneFixture.rootDirectory(), "build/config.status");
        Assert.assertTrue(configStatus.exists());
        Assert.assertTrue(result.isSuccessful());

        var result2 = builderFixture.doBuildFromScope(descriptor, scope);
        Assert.assertTrue(result2.isSuccessful());
    }

    public void testOneFileScope() throws Exception {
        gitCloneFixture.cloneDirectory();

        var model = gitCloneFixture.getJpsModel();
        builderFixture.setModel(model);
        ProjectDescriptor descriptor = builderFixture.createProjectDescriptor();

        var ll = descriptor.getBuildTargetIndex().getAllTargets()
                .stream()
                .filter(x -> x.getId().contains("sal_partial"))
                .toList();
        ll.forEach(tgt -> LOG.info("Target: " + tgt.getId() + " " + Classes.caster(HasSxForm.class).cast(tgt).map(x -> x.sxForm().asSExpression()).orElse(tgt.getClass().getName())));
        Assert.assertEquals(2, ll.size());
        var tgt1 = ll.stream().flatMap(Classes.filterAndCast(PhonyTargets.PhonyTarget.class)).findFirst().get();
        var tgt2 = ll.stream().flatMap(Classes.filterAndCast(ScriptBuildTarget.class)).findFirst().get();
        Assert.assertEquals("{aldorlib}-{base/sal_partial.as}", tgt1.getId());
        Assert.assertEquals("{root}-{lib/aldor/src/base}-{sal_partial.abn}", tgt2.getId());
    }

    public void testSalPartialBuild() throws Exception {
        gitCloneFixture.cloneDirectory();

        var model = gitCloneFixture.getJpsModel();
        builderFixture.setModel(model);
        ProjectDescriptor descriptor = builderFixture.createProjectDescriptor();
        var phonyTT = PhonyTargets.ident.findType();
        var tgt = descriptor.getBuildTargetIndex().getAllTargets(phonyTT)
                .stream()
                .filter(x -> x.descriptor().id().contains("sal_partial"))
                .findAny().orElse(null);
        Assert.assertNotNull(tgt);
        CompileScopeTestBuilder scope = CompileScopeTestBuilder.rebuild()
                .target(tgt)
                .targetTypes(ScriptBuildTargetType.ident.findType());
        var result = builderFixture.doBuild(descriptor, scope);
        Assert.assertTrue(new File(gitCloneFixture.rootDirectory(), "build/lib/aldor/src/base/sal_partial.abn").exists());
        //Assert.assertTrue(result.isSuccessful());
    }


    public void testBuild2() {

    }

    protected String getProjectName() {
        return StringUtil.decapitalize(StringUtil.trimStart(getName(), "test"));
    }


}