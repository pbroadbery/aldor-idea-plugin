package aldor.psi.index;

import aldor.build.module.AldorModuleType;
import aldor.psi.AldorDefine;
import aldor.util.JUnits;
import aldor.util.VirtualFileTests;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;


public class AldorDefineNameIndexTest extends LightPlatformCodeInsightFixtureTestCase {

    {
        JUnits.setLogToInfo();
    }

    public void testWilIndex() throws IOException {
        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        Project project = getProject();

        VirtualFile root = VirtualFileTests.getProjectRoot(project);
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "a == b; c == d; e == " + System.currentTimeMillis());

        assertTrue(FileIndexFacade.getInstance(project).isInSource(file));
        assertFalse(FileIndexFacade.getInstance(project).isExcludedFile(file));
        VirtualFileTests.deleteFile(file);
    }

    public void testDefineIndexSimpleDefs() throws IOException {
        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        Project project = getProject();
        VirtualFile root = VirtualFileTests.getProjectRoot(project);
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "a == b; c == d; e == " + System.currentTimeMillis());

        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project);
        System.out.println("Keys:" + ll);

        assertEquals(3, ll.size());
        VirtualFileTests.deleteFile(file);
    }


    public void testDefineIndexComplexDefs() throws IOException {
        Project project = getProject();
        VirtualFile root = VirtualFileTests.getProjectRoot(project);
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == stuff; aNumber == " + System.currentTimeMillis());

        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project);
        //assertEquals(Sets.newHashSet("Something", "aNumber"), new HashSet<>(ll));
        VirtualFileTests.deleteFile(file);
    }


    public void testDefineIndexGetKey() throws IOException {
        VirtualFile file = null;
        try {
            Project project = getProject();

            VirtualFile root = VirtualFileTests.getProjectRoot(project);
            file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == stuff; aNumber == " + System.currentTimeMillis());

            FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);
            Collection<String> ll = AldorDefineNameIndex.instance.getAllKeys(project);
            //assertEquals(Sets.newHashSet("Something", "aNumber"), new HashSet<>(ll));

            Collection<AldorDefine> items = AldorDefineNameIndex.instance.get("Something", getProject(), GlobalSearchScope.allScope(getProject()));
            System.out.println("Items: " + items + " " + items.iterator().next().getText());
            assertEquals(1, items.size());
            assertTrue(items.iterator().next().getText().startsWith("Something"));
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
