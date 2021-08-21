package aldor.psi.index;

import aldor.psi.AldorDefine;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.Assert;
import org.junit.Assume;

import java.util.Collection;

public class AldorDefineTopLevelIndexTest extends AssumptionAware.BasePlatformTestCase {
    private final ExecutablePresentRule rule = ExecutablePresentRule.AldorStd.INSTANCE;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Assume.assumeTrue(rule.shouldRunTest());
        JUnits.setLogToDebug();
    }

    public void testLookupDoubleFloat() {
        Collection<AldorDefine> elts = AldorDefineTopLevelIndex.instance.get("ArithmeticType", getProject(), GlobalSearchScope.allScope(getProject()));
        Assert.assertFalse(elts.isEmpty());
    }

    public void testLookupPackableType() {
        Collection<AldorDefine> elts = AldorDefineTopLevelIndex.instance.get("PackableType", getProject(), GlobalSearchScope.allScope(getProject()));
        Assert.assertFalse(elts.isEmpty());
    }

    public void testLookupFold() {
        Collection<AldorDefine> elts = AldorDefineTopLevelIndex.instance.get("Fold", getProject(), GlobalSearchScope.allScope(getProject()));
        Assert.assertFalse(elts.isEmpty());
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(ExecutablePresentRule.Aldor.INSTANCE);
    }

}