package aldor.psi.index;

import aldor.parser.SwingThreadTestRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static aldor.test_util.TestFiles.existingFile;
import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getProject;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class AldorDeclareIndexTest  {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new SwingThreadTestRule());


    @Test
    public void testIndexAnyDotSpad() throws IOException {
        Assert.assertNotNull(testFixture.getProject());

        Project project = getProject();
        File sourceFile = existingFile("/home/pab/Work/fricas/fricas/src/algebra/any.spad");

        byte[] content = Files.readAllBytes(sourceFile.toPath());

        VirtualFile file = createFile(getSourceRoot(), "any.spad", content);

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

    }

    @Test
    public void testIndexCliquesDotAS() throws IOException {
        Assert.assertNotNull(testFixture.getProject());

        Project project = getProject();
        File sourceFile = existingFile("/home/pab/Work/fricas/fricas/src/aldor/cliques.as");

        byte[] content = Files.readAllBytes(sourceFile.toPath());

        VirtualFile file = createFile(getSourceRoot(), "cliques.as", content);

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

    }


    @Test
    public void testDeclareIndex() {
        Assert.assertNotNull(testFixture.getProject());

        Project project = getProject();

        VirtualFile file = createFile(getSourceRoot(), "any.spad", "Foo: Category == with { a: % }");
        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        // Want to test
        //  - that we index only declarations within "with" statements at top level.
        //  - that forms like "Foo: Exports == Impl where ... work

    }

}
