package aldor.syntax;

import aldor.parser.EnsureParsingTest;
import aldor.parser.ParserFunctions;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

import static aldor.syntax.SyntaxPsiParser.parse;

public class SyntaxTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testId() {
        PsiElement psi = parseText("f");

        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("f", syntax.toString());
    }

    public void testParseFunctionCall() {
        PsiElement psi = parseText("f g x");

        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply f (Apply g x))", syntax.toString());
    }

    public void testParseDotFunctionCall() {
        PsiElement psi = parseText("a.f g");

        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply (Apply a f) g)", syntax.toString());
    }

    public void testParseFunctionMap() {
        PsiElement psi = parseText("() -> (Int, String)");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply -> (Comma ) (Comma Int String))", syntax.toString());
    }

    public void testParseDeclare() {
        PsiElement psi = parseText("(n: X) -> F n");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply -> (Decl n X) (Apply F n))", syntax.toString());
    }

    public void testParseInfixDeclare() {
        PsiElement psi = parseText("(a: A) + (b: B)");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply + (Decl a A) (Decl b B))", syntax.toString());
    }


    public void testParseMulti() {
        PsiElement psi = parseText("f(x: I, y: I): X");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Decl (Apply f (Comma (Decl x I) (Decl y I))) X)", syntax.toString());
    }

    public void testParseCurriedDeclare() {
        PsiElement psi = parseText("f(x: String)(y: Integer): String");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        // FIXME: This is wrong, as it should be (apply (apply f x) y)
        Assert.assertEquals("(Decl (Apply f (Decl x String) (Decl y Integer)) String)", syntax.toString());
    }


    public void testWithExpression() {
        PsiElement psi = parseText("Join(R, X) with { this: X; that: Y }");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        // FIXME: This is wrong - ought to show full type
        Assert.assertEquals("{?:Other}", syntax.toString());
    }

    public void testTupleCross() {
        PsiElement psi = parseText("Tuple Cross(K, V) -> %");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply -> (Apply Tuple (Apply Cross (Comma K V))) %)", syntax.toString());
    }

    public void testEnum() {
        PsiElement psi = parseText("'a,b,c'");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Enum a b c)", syntax.toString());
    }

    public void testQuote() {
        PsiElement psi = parseSpadText("'index");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Quote index)", syntax.toString());
    }

    public void testComplexCall() {
        PsiElement psi = parseText("DM(R: Join(A, E))");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply DM (Decl R (Apply Join (Comma A E))))", syntax.toString());
    }

    public void testDefaultArguments() {
        PsiElement psi = parseText("foo(x: I == 1)");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply foo (Define (Decl x I) Literal))", syntax.toString());
    }

    public void testDefaultArguments2() {
        PsiElement psi = parseText("foo(x: L == [])");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply foo (Define (Decl x L) (Apply bracket)))", syntax.toString());
    }


    public void testDefaultArguments3() {
        PsiElement psi = parseText("foo(a: S, x: I == 5)");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply foo (Comma (Decl a S) (Define (Decl x I) Literal)))", syntax.toString());
    }

    public void testSpadDeclare() {
        PsiElement psi = parseSpadText("foo(a: S, b: T)");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply foo (Comma (SDecl a S) (SDecl b T)))", syntax.toString());
    }

    public void testSpadExpr() {
        PsiElement psi = parseSpadText("a + b");
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        Assert.assertEquals("(Apply + a b)", syntax.toString());
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(getProject(), text);
    }

    private PsiElement parseSpadText(CharSequence text) {
        return ParserFunctions.parseSpadText(getProject(), text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }


}
