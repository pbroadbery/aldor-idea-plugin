package aldor.build.builders;

import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import aldor.util.VirtualFileTests;
import com.intellij.compiler.impl.ModuleCompileScope;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;
import org.junit.Assert;

import java.util.List;

public class AldorBuildTargetScopeProviderTest extends AssumptionAware.LightIdeaTestCase {
    private static final Logger LOG = Logger.getInstance(AldorBuildTargetScopeProviderTest.class);
    private JUnits.TearDownItem tearDown = new JUnits.TearDownItem();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tearDown.with(JUnits.setLogToDebug());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            tearDown.tearDown();
        } finally {
            super.tearDown();
        }
    }

    public void testEmptyModule() {
        AldorBuildTargetScopeProvider provider = new AldorBuildTargetScopeProvider();
        CompileScope scope = new ModuleCompileScope(getModule(), false);
        List<TargetTypeBuildScope> scopes = provider.getBuildTargetScopes(scope, getProject(), true);
        Assert.assertTrue(scopes.isEmpty());
    }

    public void testSourceFile() {
        AldorBuildTargetScopeProvider provider = new AldorBuildTargetScopeProvider();
        @NotNull VirtualFile dir = ModuleRootManager.getInstance(getModule()).getSourceRoots()[0];
        VirtualFileTests.createFile(dir, "foo.as", "never");
        CompileScope scope = new ModuleCompileScope(getModule(), false);
        List<TargetTypeBuildScope> scopes = provider.getBuildTargetScopes(scope, getProject(), true);
        LOG.info("Scopes: " + scopes.stream().map(s -> s.getTypeId() + s.getTargetIdCount() ));

        Assert.assertEquals(1, scopes.size());
        TargetTypeBuildScope fileScope = scopes.get(0);
        Assert.assertEquals(1, fileScope.getTargetIdCount());
        Assert.assertEquals("/src/foo.as", fileScope.getTargetId(0));
    }

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(ExecutablePresentRule.Aldor.INSTANCE);
    }
}