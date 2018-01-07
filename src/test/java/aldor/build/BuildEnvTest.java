package aldor.build;

import aldor.language.AldorLanguage;
import aldor.test_util.LightProjectDescriptors;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
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
        return LightProjectDescriptors.ALDOR_MODULE_DESCRIPTOR;
    }

}
