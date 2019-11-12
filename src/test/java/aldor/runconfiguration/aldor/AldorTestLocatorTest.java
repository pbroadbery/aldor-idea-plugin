package aldor.runconfiguration.aldor;

import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.execution.Location;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AldorTestLocatorTest  {
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorExecutableRule.prefix()));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.swingThreadTestRule());


    @Test
    public void testSimpleCase() {
        AldorTestLocator locator = AldorTestLocator.INSTANCE;
        String text = "Test: with blah == add { testFoo(): () == never}";
        PsiFile file = testFixture.addFileToProject("foo.as", text);

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, testFixture.getProject(), null);

        //noinspection rawtypes
        List<Location> ll = locator.getLocation("java:test", "aldor.test.Test/testFoo", testFixture.getProject(), GlobalSearchScope.fileScope(file));
        System.out.println("Loc: " + ll);

        assertEquals(1, ll.size());
        assertEquals(text.indexOf("testFoo"), ll.get(0).getPsiElement().getTextOffset());
    }

}