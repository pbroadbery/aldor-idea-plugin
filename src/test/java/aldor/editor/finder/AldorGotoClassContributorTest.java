package aldor.editor.finder;

import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.SdkProjectDescriptors;
import aldor.util.VirtualFileTests;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Assert;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class AldorGotoClassContributorTest extends AssumptionAware.BasePlatformTestCase {


    public void testGotoClass() {
        Project project = getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == stuff; aNumber == " + System.currentTimeMillis());

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);

        ChooseByNameContributor gotoClassContributor = new AldorGotoClassContributor();

        NavigationItem[] items = gotoClassContributor.getItemsByName("Something", "Something", project, false);
        Assert.assertEquals(1, items.length);
        NavigationItem item = items[0];

        Assert.assertEquals("Something", item.getName());
        Assert.assertTrue(item.canNavigate());
        VirtualFileTests.deleteFile(file);


    }

    public void testGotoClass2() {
        Project project = getProject();
        VirtualFile file = createFile(getSourceRoot(), "foo.as", "Something(x: Wibble): with == add { foo == bar }; aNumber == " + System.currentTimeMillis());

        FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);

        ChooseByNameContributor gotoClassContributor = new AldorGotoClassContributor();

        String[] names = gotoClassContributor.getNames(project, false);
        Assert.assertTrue(names.length > 2);

        NavigationItem[] items = gotoClassContributor.getItemsByName("Something", "Something", project, false);
        Assert.assertEquals(1, items.length);
        NavigationItem item = items[0];
        Assert.assertEquals("Something", item.getName());
        Assert.assertTrue(item.canNavigate());

        VirtualFileTests.deleteFile(file);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(ExecutablePresentRule.Aldor.INSTANCE);
    }
}
