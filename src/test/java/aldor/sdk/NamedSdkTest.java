package aldor.sdk;

import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SafeCloseable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.projectRoots.SimpleJavaSdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.SdkListItem;
import com.intellij.openapi.roots.ui.configuration.SdkListModelBuilder;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Condition;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NamedSdkTest {
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testNoSdk() {
        NamedSdk sdk = new NamedSdk((String)null);
        assertNull(sdk.name());
        assertNull(sdk.sdk());
    }

    @Test
    public void testSdk() {
        final Sdk someJavaSdk = new ProjectJdkImpl("SomeJdk", SimpleJavaSdkType.getInstance());

        try (SafeCloseable withSdk = JUnits.withSdk(someJavaSdk)) {
            NamedSdk namedJdk = new NamedSdk(someJavaSdk);
            assertEquals("SomeJdk", namedJdk.name());
            assertEquals(someJavaSdk, namedJdk.sdk());
        }
    }

    @Test
    public void test_ComboInit() {
        final Sdk someJavaSdk = new ProjectJdkImpl("SomeJdk", SimpleJavaSdkType.getInstance());

        try (SafeCloseable withSdk = JUnits.withSdk(someJavaSdk)) {
            NamedSdk namedJdk = new NamedSdk(someJavaSdk);
            assertEquals("SomeJdk", namedJdk.name());
            assertEquals(someJavaSdk, namedJdk.sdk());

            ProjectSdksModel model = new ProjectSdksModel();
            model.reset(null);
            JdkComboBox comboBox = new JdkComboBox(null, model,
                    (Condition<SdkTypeId>) sdkTypeId -> true,
                    (Condition<Sdk>) sdk -> true,
                    (Condition<SdkListItem.SuggestedItem>) suggestedItem -> true,
                    (Condition<SdkTypeId>) sdkTypeId -> true,
                    sdk -> {
            });
            NamedSdk.initialiseJdkComboBox(namedJdk, comboBox);
            assertEquals(someJavaSdk.getName(), Objects.requireNonNull(comboBox.getSelectedJdk()).getName());
        }
    }
}