package aldor.build;

import aldor.build.module.AldorModuleType;
import aldor.language.AldorLanguage;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

/**
 * Tests that builds work ok; note that it is incomplete at the moment.
 */
public class BuildEnvTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testThing() {
        PsiFile file = createLightFile("foo.as", AldorLanguage.INSTANCE, "Foo: with == add");
        Assert.assertNotNull(file);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new AldorBuildProjectDescriptor();
    }

    public static class AldorBuildProjectDescriptor extends LightProjectDescriptor {

        @NotNull
        @Override
        public ModuleType<?> getModuleType() {
            return AldorModuleType.instance();
        }

        @SuppressWarnings("EmptyMethod")
        @Override
        public void setUpProject(@NotNull Project project, @NotNull SetupHandler handler) throws Exception {
            super.setUpProject(project, handler);
        }

    }
}
