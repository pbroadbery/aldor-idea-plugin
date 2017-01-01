package aldor.psi.index;

import aldor.build.module.AldorModuleType;
import aldor.psi.AldorDefineStubbing;
import aldor.test_util.JUnits;
import aldor.util.VirtualFileTests;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import groovy.json.internal.Charsets;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Assume;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import static aldor.psi.AldorPsiUtils.logPsi;
import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;


@SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
public class AldorDefineNameIndexTest extends LightPlatformCodeInsightFixtureTestCase {

    {
        JUnits.setLogToInfo();
    }

    public void testWilIndex() throws IOException {
        Project project = getProject();

        VirtualFile file = createFile(getSourceRoot(), "foo.as", "a == b; c == d; e == " + System.currentTimeMillis());

        Assert.assertTrue(FileIndexFacade.getInstance(project).isInSource(file));
        Assert.assertFalse(FileIndexFacade.getInstance(project).isExcludedFile(file));
        VirtualFileTests.deleteFile(file);
    }

    public void testDefineIndexSimpleDefs() throws IOException {
        Project project = getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "a == b; c == d; e == " + System.currentTimeMillis());

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project);
        Assert.assertEquals(3, ll.size());
        VirtualFileTests.deleteFile(file);

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        Assert.assertEquals(0, AldorDefineNameIndex.instance.getAllKeys(project).size());
    }


    public void testDefineIndexComplexDefs() throws IOException {
        Project project = getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == stuff; aNumber == " + System.currentTimeMillis());

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project);
        Assert.assertEquals(Sets.newHashSet("Something", "aNumber"), new HashSet<>(ll));
        VirtualFileTests.deleteFile(file);
    }

    public void testDefineTopLevelIndex() throws IOException {
        Project project = getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == add { foo == bar }; aNumber == " + System.currentTimeMillis());

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        Collection<String> ll = AldorDefineTopLevelIndex.instance.getAllKeys(project);
        Assert.assertEquals(Sets.newHashSet("Something", "aNumber"), new HashSet<>(ll));
        VirtualFileTests.deleteFile(file);
    }


    public void testSpadDefineNameIndex() throws IOException {
        Project project = getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.spad", "Something(x: Wibble): with == add");
        logPsi(PsiManager.getInstance(project).findFile(file));
        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        Collection<String> ll = AldorDefineTopLevelIndex.instance.getAllKeys(project);
        Assert.assertEquals(Sets.newHashSet("Something"), new HashSet<>(ll));
        VirtualFileTests.deleteFile(file);
    }


    public void testDefineIndexGetKey() throws IOException {
        VirtualFile file = null;
        try {
            Project project = getProject();

            file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == stuff; aNumber == " + System.currentTimeMillis());
            FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
            FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);
            Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project);
            Assert.assertEquals(Sets.newHashSet("Something", "aNumber"), new HashSet<>(ll));

            Collection<AldorDefineStubbing.AldorDefine> items = AldorDefineNameIndex.instance.get("Something", getProject(), GlobalSearchScope.allScope(getProject()));
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

    public void testDefineFRA() throws IOException {
        Assume.assumeTrue(new File("/home/pab/IdeaProjects/fricas-codebase/fricas/src/algebra/algcat.spad").exists());
        String fraText = Files.toString(new File("/home/pab/IdeaProjects/fricas-codebase/fricas/src/algebra/algcat.spad"), Charsets.US_ASCII);
        VirtualFile file = null;
        try {
            Project project = getProject();

            file = createFile(getSourceRoot(), "algcat.spad", fraText);
            FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
            FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);
            Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project);

            System.out.println("Words: "+ ll);
            Collection<String> topLevel = AldorDefineTopLevelIndex.instance.getAllKeys(project);

            System.out.println("Top: "+ topLevel);
            assertTrue(ll.contains("FramedAlgebra"));
        }
        finally {
            if (file != null) {
                VirtualFileTests.deleteFile(file);
            }
        }
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        //noinspection ReturnOfInnerClass
        return new LightProjectDescriptor() {

            @Override
            @NotNull
            public ModuleType<?> getModuleType() {
                return AldorModuleType.instance();
            }


        };
    }
}
