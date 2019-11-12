package aldor.sdk.aldor;

import aldor.sdk.NamedSdk;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jdom.Document;
import org.jdom.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertTrue;

public class AldorSdkAdditionalDataHelperTest {
    public static final AldorSdkAdditionalDataHelper HELPER = AldorSdkAdditionalDataHelper.instance();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void test() {
        AldorSdkAdditionalData additionalData = new AldorSdkAdditionalData();
        additionalData.javaClassDirectory = "java/directory";
        additionalData.aldorUnitSdk = new NamedSdk("AldorUnit");
        additionalData.aldorUnitEnabled = true;
        Document document = new Document();
        document.setRootElement(new Element("root"));
        HELPER.saveAdditionalData(additionalData, document.getRootElement());
        AldorSdkAdditionalData additionalData2 = HELPER.loadAdditionalData(document.getRootElement());
        assertTrue(additionalData.matches(additionalData2));
    }

}