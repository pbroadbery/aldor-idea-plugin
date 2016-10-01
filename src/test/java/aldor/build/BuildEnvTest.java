package aldor.build;

import aldor.build.facet.AldorFacet;
import aldor.build.facet.AldorFacetType;
import aldor.language.AldorLanguage;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * Tests that builds work ok; note that it is incomplete at the moment.
 */
public class BuildEnvTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testThing() {
        Project project = getProject();
        PsiFile file = createLightFile("foo.as", AldorLanguage.INSTANCE, "Foo: with == add");
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new AldorBuildProjectDescriptor();
    }

    public static class AldorBuildProjectDescriptor extends LightProjectDescriptor {

        @SuppressWarnings("EmptyMethod")
        @Override
        public void setUpProject(@NotNull Project project, @NotNull SetupHandler handler) throws Exception {
            super.setUpProject(project, handler);
        }

        @Override
        protected void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
            final AldorFacet facet = FacetManager.getInstance(module).addFacet(AldorFacetType.instance(), "AppEngine", null);
        }

    }
}
