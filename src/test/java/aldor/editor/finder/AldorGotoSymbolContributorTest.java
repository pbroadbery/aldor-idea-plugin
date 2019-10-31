package aldor.editor.finder;

import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SkipCI;
import aldor.test_util.SkipOnCIBuildRule;
import aldor.util.VirtualFileTests;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static aldor.psi.AldorPsiUtils.logPsi;
import static aldor.test_util.LightProjectDescriptors.ALDOR_MODULE_DESCRIPTOR;
import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public final class AldorGotoSymbolContributorTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(this.getProjectDescriptor());
    private final AnnotationFileTestFixture annotationTestFixture = new AnnotationFileTestFixture();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(annotationTestFixture.rule(testFixture::getProject))
                    .around(JUnits.swingThreadTestRule());

    @Rule
    public final TestRule rule = new SkipOnCIBuildRule();

    @Before
    public void setUp() {
        JUnits.setLogToDebug();
    }

    @Test
    public void testGotoSymbol() {
        Project project = testFixture.getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something: with == add { aNumber == " + System.currentTimeMillis() + "}");

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        ChooseByNameContributor gotoSymbolContributor = new AldorGotoSymbolContributor();

        String[] names = gotoSymbolContributor.getNames(project, false);
        Assert.assertEquals(2, names.length);

        NavigationItem[] items = gotoSymbolContributor.getItemsByName("aNumber", "aNumber", project, false);
        Assert.assertEquals(1, items.length);
        NavigationItem item = items[0];
        Assert.assertTrue(item.canNavigate());
        Assert.assertEquals("aNumber", item.getName());
        VirtualFileTests.deleteFile(file);
    }

    @SkipCI
    @Test
    @Ignore("Broken - becuase we index definitions, not declarations")
    public void testGotoSymbolDeclareCategory() {
        Project project = testFixture.getProject();
        VirtualFile file = createFile(testFixture.getProject().getBaseDir(), "foo.as", String.format("Something: Category == with { foo: String_%s }", System.currentTimeMillis()));

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        ChooseByNameContributor gotoSymbolContributor = new AldorGotoSymbolContributor();

        NavigationItem[] items = gotoSymbolContributor.getItemsByName("foo", "foo", project, false);

        Assert.assertEquals(1, items.length);
        Assert.assertTrue(items[0].canNavigate());
    }

    @SkipCI
    @Test
    @Ignore("Broken - becuase we index definitions, not declarations")
    public void testGotoSymbolDeclareDomain() {
        Project project = testFixture.getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", String.format("Something: X_%s with { foo: %% }  == add {}", System.currentTimeMillis()));

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        ChooseByNameContributor gotoSymbolContributor = new AldorGotoSymbolContributor();

        NavigationItem[] items = gotoSymbolContributor.getItemsByName("foo", "foo", project, false);
        PsiFile psi = PsiManager.getInstance(project).findFile(file);
        logPsi(psi);
        Assert.assertEquals(1, items.length);
        Assert.assertTrue(items[0].canNavigate());
    }

    @SkipCI
    @Test
    @Ignore("Broken - becuase we index definitions, not declarations")
    public void testGotoSymbolDeclareMacroDomain() {
        Project project = testFixture.getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", String.format("Something: E == I where E ==> X_%s with { foo: %% } I ==> add", System.currentTimeMillis()));

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        ChooseByNameContributor gotoSymbolContributor = new AldorGotoSymbolContributor();

        NavigationItem[] items = gotoSymbolContributor.getItemsByName("foo", "foo", project, false);
        PsiFile psi = PsiManager.getInstance(project).findFile(file);
        logPsi(psi);

        Assert.assertEquals(1, items.length);
        Assert.assertTrue(items[0].canNavigate());
    }

    private LightProjectDescriptor getProjectDescriptor() {
        return ALDOR_MODULE_DESCRIPTOR;
    }

}
