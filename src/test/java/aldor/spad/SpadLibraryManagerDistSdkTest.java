package aldor.spad;

import aldor.test_util.AssumptionAware;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.fricasSdkProjectDescriptor;
import static org.junit.Assert.assertTrue;

public class SpadLibraryManagerDistSdkTest extends SpadLibraryManagerTestCase {

    @Rule
    public final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-linux-gnu");

    private final CodeInsightTestFixture testFixture = createFixture(fricasSdkProjectDescriptor(directory));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(directory)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Override
    public String basePath() {
        return directory.path();
    }

    @Override
    public CodeInsightTestFixture testFixture() {
        return testFixture;
    }

    @Test
    public void test() {
        //noinspection ConstantJUnitAssertArgument
        assertTrue(true);
    }
}
