package aldor.spad;

import aldor.parser.SwingThreadTestRule;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static aldor.test_util.SdkProjectDescriptors.fricasSdkProjectDescriptor;

public class SpadLibraryManagerDistSdkTest extends SpadLibraryManagerTestCase {

    @Rule
    public final DirectoryPresentRule directory = new DirectoryPresentRule("/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-unknown-linux");

    private final CodeInsightTestFixture testFixture = createFixture(fricasSdkProjectDescriptor(directory.path()));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(directory)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new SwingThreadTestRule());

    @Override
    public String basePath() {
        return directory.path();
    }

    @Override
    public CodeInsightTestFixture testFixture() {
        return testFixture;
    }
}
