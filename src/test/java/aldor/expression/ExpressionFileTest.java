package aldor.expression;

import aldor.build.module.AldorModuleType;
import aldor.util.VirtualFileTests;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class ExpressionFileTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testIndexing() {
        Project project = getProject();
        VirtualFile root = VirtualFileTests.getProjectRoot(project);
        VirtualFile file = createFile(getSourceRoot(), "foo2.expr", "a == b; c == d");
        assertTrue(FileIndexFacade.getInstance(project).isInSource(file));
        assertFalse(FileIndexFacade.getInstance(project).isExcludedFile(file));

        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        Collection<String> ll = ExpressionDefineStubIndex.instance.getAllKeys(project);
        assertEquals(2, ll.size());
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

