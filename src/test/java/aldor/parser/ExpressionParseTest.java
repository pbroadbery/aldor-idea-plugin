package aldor.parser;

import aldor.test_util.AssumptionAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.List;

import static aldor.psi.AldorPsiUtils.logPsi;

public class ExpressionParseTest extends AssumptionAware.LightPlatformCodeInsightFixtureTestCase {

    public void testParseFunctionCall() {
        String text = "f g x";
        PsiElement psi = ParserFunctions.parseAldorText(getProject(), text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    public void testParseEnum() {
        String text = "Record(type: 'sym,number,str,ws,oparen,cparen,dot,error', txt: String)";
        PsiElement psi = ParserFunctions.parseAldorText(getProject(), text);
        logPsi(psi);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }


}
