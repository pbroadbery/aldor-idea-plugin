package aldor.psi.index;

import aldor.parser.NavigatorFactory;
import aldor.psi.AldorDeclare;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AldorDeclareIndexTopLevelTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.swingThreadTestRule());


    @Test
    public void testSimpleTopLevel() {
        assertNotNull(testFixture.getProject());
        Project project = testFixture.getProject();

        VirtualFile file = createFile(getSourceRoot(), "simpleTopLevel.as", "Foo: with { x: String } == add { x: String == 2}");

        //FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);

        Collection<String> ll = AldorDeclareTopIndex.instance.getAllKeys(project);

        Optional<String> xName = ll.stream().filter("x"::equals).findFirst();
        assertTrue(xName.isPresent());

        Collection<AldorDeclare> decl = AldorDeclareTopIndex.instance.get("x", project, GlobalSearchScope.fileScope(project, file));
        assertEquals(1, decl.size());
    }

    @Test
    public void testInfixDefinitions() {
        assertNotNull(testFixture.getProject());
        Project project = testFixture.getProject();

        VirtualFile file = createFile(getSourceRoot(), "infix.spad", "QQ: SetCategory with\n" +
                "    \"*\" : (%, %) -> %\n" +
                "== add");

        Collection<String> ll = AldorDeclareTopIndex.instance.getAllKeys(project);

        Optional<String> xName = ll.stream().filter("*"::equals).findFirst();
        assertTrue("Error: "+ll, xName.isPresent());

        Collection<AldorDeclare> declList = AldorDeclareTopIndex.instance.get("*", project, GlobalSearchScope.fileScope(project, file));
        assertEquals(1, declList.size());

        AldorDeclare decl = declList.iterator().next();
        NavigationItem nav = NavigatorFactory.get(project).getNavigationItem(decl);
        assertNotNull(nav.getPresentation());
        assertEquals("*: (%, %) -> %", nav.getPresentation().getPresentableText());
    }

}
