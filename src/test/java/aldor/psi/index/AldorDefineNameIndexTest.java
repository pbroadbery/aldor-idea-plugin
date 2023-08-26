package aldor.psi.index;

import aldor.psi.AldorDefine;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.LightProjectDescriptors;
import aldor.util.VirtualFileTests;
import com.google.common.collect.Sets;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import static aldor.psi.AldorPsiUtils.logPsi;
import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;
import static java.nio.file.Files.readAllLines;

public final class AldorDefineNameIndexTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor());

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.swingThreadTestRule());


    @Test
    public void testWilIndex() {
        Project project = testFixture.getProject();

        VirtualFile file = createFile(getSourceRoot(), "foo.as", "a == b; c == d; e == " + System.currentTimeMillis());

        Assert.assertTrue(FileIndexFacade.getInstance(project).isInSource(file));
        Assert.assertFalse(FileIndexFacade.getInstance(project).isExcludedFile(file));
        VirtualFileTests.deleteFile(file);
    }

    @Test
    public void testDefineIndexSimpleDefs() {
        Project project = testFixture.getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "a == b; c == d; e == " + System.currentTimeMillis());

        Collection<String> ll = AldorDefineTopLevelIndex.instance.getAllKeys(project, GlobalSearchScope.fileScope(project, file));
        Assert.assertEquals(3, ll.size());

        VirtualFileTests.deleteFile(file);

        Assert.assertEquals(0, AldorDefineTopLevelIndex.instance.getAllKeys(project, GlobalSearchScope.fileScope(project, file)).size());
    }


    @Test
    public void testDefineIndexComplexDefs() {
        Project project = testFixture.getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == stuff; aNumber == " + System.currentTimeMillis());

        try {
            Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project, GlobalSearchScope.fileScope(project, file));
            Assert.assertEquals(Sets.newHashSet("Something", "aNumber"), new HashSet<>(ll));
        }
        finally {
            VirtualFileTests.deleteFile(file);
        }
    }

    @Test
    public void testDefineTopLevelIndex() {
        Project project = testFixture.getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == add { foo == bar }; aNumber == " + System.currentTimeMillis());
        try {

            Collection<String> ll = AldorDefineTopLevelIndex.instance.getAllKeys(project, GlobalSearchScope.fileScope(project, file));
            Assert.assertEquals(Sets.newHashSet("Something", "aNumber"), new HashSet<>(ll));
        } finally {
            VirtualFileTests.deleteFile(file);
        }
    }


    @Test
    public void testSpadDefineNameIndex() {
        Project project = testFixture.getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.spad", "Something(x: Wibble): with == add");
        try {
            logPsi(PsiManager.getInstance(project).findFile(file));

            Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project, GlobalSearchScope.fileScope(project, file));
            Assert.assertEquals(Sets.newHashSet("Something"), new HashSet<>(ll));
        } finally {
            VirtualFileTests.deleteFile(file);
        }
    }


    @Test
    public void testDefineIndexGetKey() throws IOException {
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == stuff; aNumber == " + System.currentTimeMillis());
        try {
            Project project = testFixture.getProject();

            Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project, GlobalSearchScope.fileScope(project, file));

            Assert.assertEquals(Sets.newHashSet("Something", "aNumber"), new HashSet<>(ll));

            Collection<AldorDefine> items = AldorDefineNameIndex.instance.get("Something", testFixture.getProject(),
                    GlobalSearchScope.allScope(testFixture.getProject()));
            System.out.println("Items: " + items + " " + items.iterator().next().getText());
            Assert.assertEquals(1, items.size());
            Assert.assertTrue(items.iterator().next().getText().startsWith("Something"));
        }
        finally {
            if (file != null) {
                VirtualFileTests.deleteFile(file);
            }
        }
    }

    @Test
    public void testDefineFRA() throws IOException {
        Assume.assumeTrue(new File("/home/pab/IdeaProjects/fricas-codebase/fricas/src/algebra/algcat.spad").exists());
        String fraText = String.join("\n", readAllLines(Paths.get("/home/pab/IdeaProjects/fricas-codebase/fricas/src/algebra/algcat.spad"), StandardCharsets.US_ASCII));
        VirtualFile file = null;
        try {
            Project project = testFixture.getProject();

            file = createFile(getSourceRoot(), "algcat.spad", fraText);

            Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project, GlobalSearchScope.fileScope(project, file));

            System.out.println("Words: "+ ll);
            Collection<String> topLevel = AldorDefineTopLevelIndex.instance.getAllKeys(project);

            System.out.println("Top: "+ topLevel);
            Assert.assertTrue(ll.contains("FramedAlgebra"));
        }
        finally {
            if (file != null) {
                VirtualFileTests.deleteFile(file);
            }
        }
    }

    private static LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptors.ALDOR_MODULE_DESCRIPTOR;
    }
}
