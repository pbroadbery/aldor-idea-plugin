package pab.aldor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.util.List;

public class ExpressionParseTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testParseFunctionCall() {
        String text = "f g x";
        PsiElement psi = ParserFunctions.parseText(getProject(), text);
        ParserFunctions.logPsi(psi, 0);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    public void testParseEnum() {
        String text = "Record(type: 'sym,number,str,ws,oparen,cparen,dot,error', txt: String)";
        PsiElement psi = ParserFunctions.parseText(getProject(), text);
        ParserFunctions.logPsi(psi, 0);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }


}
