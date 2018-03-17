package aldor.sdk;

import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AldorInstalledSdkTypeTest {

    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(null);
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""));

    @Test
    public void testAldorSdk() {
        SdkType sdk = new AldorInstalledSdkType();
        assertTrue(sdk.isValidSdkHome(aldorExecutableRule.prefix()));
    }

    @Test
    public void testVersionString() {
        SdkType sdk = new AldorInstalledSdkType();
        assertNotNull(sdk.getVersionString(aldorExecutableRule.prefix()));
    }

    @Test
    public void testSuggestSdkName() {
        SdkType sdk = new AldorInstalledSdkType();
        assertNotNull(sdk.getVersionString(aldorExecutableRule.prefix()));
    }


    @Test
    public void testSetupPaths() {
        AldorInstalledSdkType sdkType = new AldorInstalledSdkType();
        Sdk sdk = new ProjectJdkImpl("Fricas Test SDK", sdkType);

        SdkModificator mod = sdk.getSdkModificator();
        mod.setHomePath(aldorExecutableRule.prefix());

        mod.commitChanges();
        assertEquals(sdk.getHomePath(), aldorExecutableRule.prefix());
        sdkType.setupSdkPaths(sdk);

        VirtualFile[] srcs = sdk.getRootProvider().getFiles(OrderRootType.SOURCES);
        assertTrue(srcs.length > 0);
        Disposer.dispose((Disposable) sdk);
    }

}
