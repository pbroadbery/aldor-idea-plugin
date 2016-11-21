package aldor.editor;

import aldor.build.module.AldorModuleType;
import aldor.util.VirtualFileTests;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class AldorGotoClassContributorTest extends LightPlatformCodeInsightFixtureTestCase {


    public void testGotoClass() throws IOException {
        Project project = getProject();
        createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == stuff; aNumber == " + System.currentTimeMillis());

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        ChooseByNameContributor gotoClassContributor = new AldorGotoClassContributor();

        String[] names = gotoClassContributor.getNames(project, false);
        assertEquals(2, names.length);

        NavigationItem[] items = gotoClassContributor.getItemsByName("Something", "Something", project, false);
        assertEquals(1, items.length);
        NavigationItem item = items[0];
        assertEquals("Something", item.getName());
    }

    public void testGotoClass2() throws IOException {
        Project project = getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == add { foo == bar }; aNumber == " + System.currentTimeMillis());

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
        FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

        ChooseByNameContributor gotoClassContributor = new AldorGotoClassContributor();

        String[] names = gotoClassContributor.getNames(project, false);
        assertEquals(2, names.length);

        NavigationItem[] items = gotoClassContributor.getItemsByName("Something", "Something", project, false);
        assertEquals(1, items.length);
        NavigationItem item = items[0];
        assertEquals("Something", item.getName());
        VirtualFileTests.deleteFile(file);
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
