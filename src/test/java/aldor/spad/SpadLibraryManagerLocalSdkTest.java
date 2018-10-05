package aldor.spad;

import aldor.parser.SwingThreadTestRule;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.fricasLocalSdkProjectDescriptor;

public class SpadLibraryManagerLocalSdkTest extends SpadLibraryManagerTestCase {


    private final DirectoryPresentRule directoryPresentRule = new DirectoryPresentRule("/home/pab/tmp/plugin/test/fricas_git");
    private final CodeInsightTestFixture testFixture = createFixture(fricasLocalSdkProjectDescriptor(directoryPresentRule.path()));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(directoryPresentRule)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new SwingThreadTestRule());

    @Override
    public String basePath() {
        return directoryPresentRule.path();
    }

    @Override
    public CodeInsightTestFixture testFixture() {
        return testFixture;
    }
}
