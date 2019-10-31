package aldor.sdk.fricas;

import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SafeCloseable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FricasInstalledSdkTypeTest {
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(null);
    private final ExecutablePresentRule fricasExecutableRule = new ExecutablePresentRule.Fricas();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(fricasExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testFricasSdk() {
        SdkType sdk = new FricasInstalledSdkType();
        assertTrue(sdk.isValidSdkHome(fricasExecutableRule.prefix()));
    }

    @Test
    public void testVersionString() {
        SdkType sdk = new FricasInstalledSdkType();
        assertNotNull(sdk.getVersionString(fricasExecutableRule.prefix()));
    }


    @Test
    public void testSetupPaths() {
        FricasInstalledSdkType sdkType = new FricasInstalledSdkType();
        Sdk sdk = new ProjectJdkImpl("Fricas Test SDK", sdkType);
        try (SafeCloseable c = JUnits.withSdk(sdk)) {
            SdkModificator mod = sdk.getSdkModificator();
            mod.setHomePath(fricasExecutableRule.prefix());
            mod.commitChanges();
            assertEquals(sdk.getHomePath(), fricasExecutableRule.prefix());
            sdkType.setupSdkPaths(sdk);

            VirtualFile[] srcs = sdk.getRootProvider().getFiles(OrderRootType.SOURCES);
            assertTrue(srcs.length > 0);
        }
    }

}
