package aldor.sdk.aldorunit;

import aldor.sdk.NamedSdk;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SafeCloseable;
import aldor.test_util.Swings;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SimpleJavaSdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import javax.swing.JComponent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AldorUnitSdkDataConfigurableTest {
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testNullSdk() {
        AldorUnitSdkDataConfigurable configurable = new AldorUnitSdkDataConfigurable();
        configurable.setSdk(null);
        assertFalse(configurable.isModified());
        JComponent component = configurable.createComponent();
        assertNotNull(component);
        JdkComboBox combo = Swings.findChild(component, JdkComboBox.class).orElseThrow(RuntimeException::new);
        assertNotNull(combo.getSelectedItem());
        assertEquals("<Unknown>", combo.getSelectedItem().getSdkName());

    }

    @Test
    public void testRealSdk_init_no_data() {
        AldorUnitSdkDataConfigurable configurable = new AldorUnitSdkDataConfigurable();
        Sdk theSdk = new ProjectJdkImpl("AldorUnit SDK", AldorUnitSdkType.instance());
        configurable.setSdk(theSdk);
        assertFalse(configurable.isModified());
        JComponent component = configurable.createComponent();
        JdkComboBox combo = Swings.findChild(component, JdkComboBox.class).orElseThrow(RuntimeException::new);
        assertNotNull(combo.getSelectedItem());
        assertEquals("<Unknown>", combo.getSelectedItem().getSdkName());
    }

    @Test
    public void testRealSdk_initInvalid() {
        AldorUnitAdditionalData additionalData = new AldorUnitAdditionalData();
        additionalData.jdk = new NamedSdk("not-an-sdk");
        Sdk theSdk = new ProjectJdkImpl("AldorUnit SDK", AldorUnitSdkType.instance());
        SdkModificator sdkModificator = theSdk.getSdkModificator();
        sdkModificator.setSdkAdditionalData(additionalData);
        sdkModificator.commitChanges();

        AldorUnitSdkDataConfigurable configurable = new AldorUnitSdkDataConfigurable();
        configurable.setSdk(theSdk);
        assertFalse(configurable.isModified());

        JComponent component = configurable.createComponent();
        JdkComboBox combo = Swings.findChild(component, JdkComboBox.class).orElseThrow(RuntimeException::new);
        assertNotNull(combo.getSelectedItem());
        assertEquals("not-an-sdk", combo.getSelectedItem().getSdkName());
    }

    @Test
    public void testRealSdk_initValid() {
        final Sdk someJavaSdk = new ProjectJdkImpl("SomeJDK", SimpleJavaSdkType.getInstance());
        try (SafeCloseable any = JUnits.withSdk(someJavaSdk)) {
            AldorUnitAdditionalData additionalData = new AldorUnitAdditionalData();
            additionalData.jdk = new NamedSdk(someJavaSdk);
            Sdk theSdk = new ProjectJdkImpl("AldorUnit SDK", AldorUnitSdkType.instance());
            SdkModificator sdkModificator = theSdk.getSdkModificator();
            sdkModificator.setSdkAdditionalData(additionalData);
            sdkModificator.commitChanges();

            AldorUnitSdkDataConfigurable configurable = new AldorUnitSdkDataConfigurable();
            configurable.setSdk(theSdk);
            assertFalse(configurable.isModified());

            JComponent component = configurable.createComponent();
            JdkComboBox combo = Swings.findChild(component, JdkComboBox.class).orElseThrow(RuntimeException::new);
            assertNotNull(combo.getSelectedItem());
            assertEquals("SomeJDK", combo.getSelectedItem().getSdkName());
        }
    }

    @Test
    public void testModify() {
        final Sdk someJavaSdk = new ProjectJdkImpl("SomeJDK", SimpleJavaSdkType.getInstance());
        try (SafeCloseable any = JUnits.withSdk(someJavaSdk)) {
            Sdk theSdk = new ProjectJdkImpl("AldorUnit SDK", AldorUnitSdkType.instance());
            AldorUnitSdkDataConfigurable configurable = new AldorUnitSdkDataConfigurable();
            configurable.setSdk(theSdk);
            assertFalse(configurable.isModified());

            JComponent component = configurable.createComponent();
            JdkComboBox combo = Swings.findChild(component, JdkComboBox.class).orElseThrow(RuntimeException::new);
            combo.setSelectedJdk(someJavaSdk);
            assertTrue(configurable.isModified());

            configurable.apply();
            assertFalse(configurable.isModified());
            AldorUnitAdditionalData data = (AldorUnitAdditionalData) theSdk.getSdkAdditionalData();
            assertNotNull(data);
            Sdk sdk = data.jdk.sdk();
            assertNotNull(sdk);
            assertEquals(someJavaSdk.getName(), sdk.getName());
        }
    }

}