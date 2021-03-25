package aldor.build;

import aldor.language.AldorLanguage;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.LightProjectDescriptors;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Tests that builds work ok; note that it is incomplete at the moment.
 */
public class BuildEnvTest extends AssumptionAware.BasePlatformTestCase {

    public void testThing() {
        PsiFile file = createLightFile("foo.as", AldorLanguage.INSTANCE, "Foo: with == add");
        Assume.assumeFalse(true);
        Assert.assertNotNull(file);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(ExecutablePresentRule.AldorDev.INSTANCE);
    }
}
