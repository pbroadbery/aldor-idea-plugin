package aldor.sdk.aldor;

import aldor.psi.AldorDefine;
import aldor.psi.index.AldorDefineNameIndex;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collection;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AldorLocalSdkTest {
    private final DirectoryPresentRule directoryPresentRule = new DirectoryPresentRule("/home/pab/tmp/plugin/test/aldor_git");
    private final CodeInsightTestFixture testFixture = createFixture(SdkProjectDescriptors.aldorLocalSdkProjectDescriptor(directoryPresentRule.path()));

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(directoryPresentRule)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testFilesAndIndices() {
        // First time round, lets see if things are indexed nicely.
        Project project = testFixture.getProject();
        PsiFile[] files = FilenameIndex.getFilesByName(project, "sal_lang.as", GlobalSearchScopesCore.projectProductionScope(project));

        assertEquals(1, files.length);

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        Collection<AldorDefine> ll = AldorDefineNameIndex.instance.get("Category", project, GlobalSearchScope.allScope(project));
        assertFalse(ll.isEmpty());
    }

}
