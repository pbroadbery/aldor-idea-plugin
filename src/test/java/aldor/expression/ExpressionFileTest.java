package aldor.expression;

import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import org.junit.Assert;
import org.junit.Assume;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class ExpressionFileTest extends AssumptionAware.BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnits.setLogToInfo();
    }

    public void testIndexing() {
        Project project = getProject();

        VirtualFile file = createFile(getSourceRoot(), "foo2.expr", "a == b; c == d");
        Assert.assertTrue(FileIndexFacade.getInstance(project).isInSource(file));
        Assert.assertFalse(FileIndexFacade.getInstance(project).isExcludedFile(file));

        Collection<String> ll = ExpressionDefineStubIndex.instance.getAllKeys(project);
        Assert.assertEquals(2, ll.size());
        Assert.assertEquals(Set.of("a == b", "c == d"), new HashSet<>(ExpressionDefineStubIndex.instance.getAllKeys(project)));
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        ExecutablePresentRule.Aldor aldorRule = new ExecutablePresentRule.Aldor();
        Assume.assumeTrue(aldorRule.shouldRunTest());
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorRule.prefix());
    }

}

