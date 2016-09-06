package pab.aldor;

import aldor.AldorPsiUtils;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class SyntaxTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testId() {
        String text = "f";
        PsiElement psi = parseText(text);
        ParserFunctions.logPsi(psi, 0);

        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);

        assertEquals("f", syntax.toString());
    }

    public void testParseFunctionCall() {
        String text = "f g x";
        PsiElement psi = parseText(text);
        ParserFunctions.logPsi(psi, 0);

        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);

        assertEquals("(Apply f (Apply g x))", syntax.toString());
    }


    public void testParseDotFunctionCall() {
        String text = "a.f g";
        PsiElement psi = parseText(text);
        ParserFunctions.logPsi(psi, 0);

        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);

        assertEquals("(Apply (Apply a f) g)", syntax.toString());
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseText(getProject(), text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }


}
