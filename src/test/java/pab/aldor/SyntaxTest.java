package pab.aldor;

import aldor.AldorPsiUtils;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static pab.aldor.ParserFunctions.logPsi;

public class SyntaxTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testId() {
        PsiElement psi = parseText("f");

        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);
        assertNotNull(syntax);
        assertEquals("f", syntax.toString());
    }

    public void testParseFunctionCall() {
        PsiElement psi = parseText("f g x");

        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);
        assertNotNull(syntax);
        assertEquals("(Apply f (Apply g x))", syntax.toString());
    }

    public void testParseDotFunctionCall() {
        PsiElement psi = parseText("a.f g");

        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);
        assertNotNull(syntax);
        assertEquals("(Apply (Apply a f) g)", syntax.toString());
    }

    public void testParseFunctionMap() {
        PsiElement psi = parseText("() -> (Int, String)");
        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);
        assertNotNull(syntax);
        assertEquals("(InfixApply -> (Comma ) (Comma Int String))", syntax.toString());
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseText(getProject(), text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }


}
