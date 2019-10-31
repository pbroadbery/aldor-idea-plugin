package aldor.sdk.aldorunit;

import aldor.sdk.NamedSdk;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jdom.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AldorUnitSdkTypeTest {
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testSdkLocation() throws IOException {
        AldorUnitSdkType sdk = AldorUnitSdkType.instance();
        Path tmpDir = Files.createTempDirectory("fake-aldorunit-");
        tmpDir.toFile().deleteOnExit();
        assertFalse(sdk.isValidSdkHome(tmpDir.toString()));

        assertTrue(new File(tmpDir.toFile(), "aldorunit.jar").createNewFile());
        assertTrue(sdk.isValidSdkHome(tmpDir.toString()));
    }

    @Test
    public void testSdkSave_Empty() {
        AldorUnitSdkType sdkType = AldorUnitSdkType.instance();

        Element element = new Element("data");
        AldorUnitAdditionalData additionalData = new AldorUnitAdditionalData();
        sdkType.saveAdditionalData(additionalData, element);
        AldorUnitAdditionalData newData = (AldorUnitAdditionalData) sdkType.loadAdditionalData(element);
        assertNotNull(newData);
        assertEquals(additionalData.jdk, newData.jdk);
    }

    @Test
    public void testSdkSave_Setting() {
        AldorUnitSdkType sdkType = AldorUnitSdkType.instance();

        Element element = new Element("data");
        AldorUnitAdditionalData additionalData = new AldorUnitAdditionalData();
        additionalData.jdk = new NamedSdk("SomeJdk");
        sdkType.saveAdditionalData(additionalData, element);
        AldorUnitAdditionalData newData = (AldorUnitAdditionalData) sdkType.loadAdditionalData(element);
        assertNotNull(newData);
        assertEquals(additionalData.jdk, newData.jdk);
    }

}