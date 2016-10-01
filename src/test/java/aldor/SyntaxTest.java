package aldor;

import aldor.parser.EnsureParsingTest;
import aldor.parser.ParserFunctions;
import aldor.psi.AldorPsiUtils;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

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

    public void testParseDeclare() {
        PsiElement psi = parseText("(n: X) -> F n");
        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);
        assertNotNull(syntax);
        assertEquals("(InfixApply -> (Decl n X) (Apply F n))", syntax.toString());
    }

    public void testParseInfixDeclare() {
        PsiElement psi = parseText("(a: A) + (b: B)");
        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);
        assertNotNull(syntax);
        assertEquals("(InfixApply + (Decl a A) (Decl b B))", syntax.toString());
    }

    public void testParseCurriedDeclare() {
        PsiElement psi = parseText("f(x: String)(y: Integer): String");
        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);
        assertNotNull(syntax);
        // FIXME: This is wrong, as it should be (apply (apply f x) y)
        assertEquals("(Decl (Apply f (Decl x String) (Decl y Integer)) String)", syntax.toString());
    }


    public void testWithExpression() {
        PsiElement psi = parseText("Join(R, X) with { this: X; that: Y }");
        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);
        assertNotNull(syntax);
        // FIXME: This is wrong - ought to show full type
        assertEquals("(Other)", syntax.toString());
    }

    public void testTupleCross() {
        PsiElement psi = parseText("Tuple Cross(K, V) -> %");
        AldorPsiUtils.Syntax syntax = AldorPsiUtils.parse(psi);
        assertNotNull(syntax);
        // FIXME: This is wrong - ought to show full type
        assertEquals("(InfixApply -> (Apply Tuple (Apply Cross (Comma K V))) %)", syntax.toString());
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseText(getProject(), text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }


}
