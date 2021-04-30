package aldor.build.builders;

import aldor.builder.AldorBuildConstants;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.compiler.impl.ModuleCompileScope;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;
import org.junit.Assert;

import java.util.List;

public class AldorBuildTargetScopeProviderTest extends AssumptionAware.LightIdeaTestCase {

    public void testEmptyModule() {
        AldorBuildTargetScopeProvider provider = new AldorBuildTargetScopeProvider();
        CompileScope scope = new ModuleCompileScope(getModule(), false);
        List<TargetTypeBuildScope> scopes = provider.getBuildTargetScopes(scope, getProject(), true);
        System.out.println("Scopes: " + scopes);
        Assert.assertEquals(1, scopes.size());
        Assert.assertEquals("ALDOR_JAR_TARGET", scopes.get(0).getTypeId());
    }

    public void testSourceFile() {
        AldorBuildTargetScopeProvider provider = new AldorBuildTargetScopeProvider();
        createLightFile("foo.as", "never");

        CompileScope scope = new ModuleCompileScope(getModule(), false);
        List<TargetTypeBuildScope> scopes = provider.getBuildTargetScopes(scope, getProject(), true);
        System.out.println("Scopes: " + scopes);

        Assert.assertEquals(2, scopes.size());
        TargetTypeBuildScope fileScope = scopes.stream().filter(s -> s.getTypeId().equals(AldorBuildConstants.ALDOR_FILE_TARGET)).findAny().orElseThrow();
        Assert.assertEquals(1, fileScope.getTargetIdCount());
    }

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(ExecutablePresentRule.Aldor.INSTANCE);
    }
}