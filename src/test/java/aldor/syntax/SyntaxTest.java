package aldor.syntax;

import aldor.parser.ParserFunctions;
import aldor.test_util.AssumptionAware;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

import static aldor.syntax.SyntaxPsiParser.parse;

public class SyntaxTest extends AssumptionAware.BasePlatformTestCase {

    @SuppressWarnings("UnnecessaryCodeBlock")
    public void testParser1() {
        {
            PsiElement psi = parseText("f");

            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("f", syntax.toString());
        }

        {
            PsiElement psi = parseText("f g x");

            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply f (Apply g x))", syntax.toString());
        }

        {
            PsiElement psi = parseText("a.f g");

            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply (Apply a f) g)", syntax.toString());
        }

        {
            PsiElement psi = parseText("() -> (Int, String)");

            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply -> (Comma ) (Comma Int String))", syntax.toString());
        }


    /* testParseDeclare() */
        {
            PsiElement psi = parseText("(n: X) -> F n");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply -> (Decl n X) (Apply F n))", syntax.toString());
        }

    /* testParseInfixDeclare() */
        {
            PsiElement psi = parseText("(a: A) + (b: B)");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply + (Decl a A) (Decl b B))", syntax.toString());
        }
    }

    @SuppressWarnings("UnnecessaryCodeBlock")
    public void testParser2() {

    /* testParseMulti() */
        {
            PsiElement psi = parseText("f(x: I, y: I): X");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Decl (Apply f (Decl x I) (Decl y I)) X)", syntax.toString());
        }

    /* testParseCurriedDeclare() */
        {
            PsiElement psi = parseText("f(x: String)(y: Integer): String");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            // FIXME: This is wrong, as it should be (apply (apply f x) y)
            Assert.assertEquals("(Decl (Apply f (Decl x String) (Decl y Integer)) String)", syntax.toString());
        }


    /* testWithExpression() */
        {
            PsiElement psi = parseText("Join(R, X) with { this: X; that: Y }");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            // FIXME: This is wrong - ought to show full type
            Assert.assertEquals("{?:With}", syntax.toString());
        }
    }

    @SuppressWarnings("UnnecessaryCodeBlock")
    public void testParser3() {
    /* testTupleCross() */
        {
            PsiElement psi = parseText("Tuple Cross(K, V) -> %");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply -> (Apply Tuple (Apply Cross (Comma K V))) %)", syntax.toString());
        }

    /* testEnum() */
        {
            PsiElement psi = parseText("'a,b,c'");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Enum a b c)", syntax.toString());
        }

    /* testQuote() */
        {
            PsiElement psi = parseSpadText("'index");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Quote index)", syntax.toString());
        }

    /* testComplexCall() */
        {
            PsiElement psi = parseText("DM(R: Join(A, E))");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply DM (Decl R (Apply Join A E)))", syntax.toString());
        }

    /* testDefaultArguments() */
        {
            PsiElement psi = parseText("foo(x: I == 1)");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply foo (Define (Decl x I) 1))", syntax.toString());
        }

    /* testDefaultArguments2() */
        {
            PsiElement psi = parseText("foo(x: L == [])");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply foo (Define (Decl x L) (Apply bracket)))", syntax.toString());
        }
    }

    @SuppressWarnings("UnnecessaryCodeBlock")
    public void testParser4() {

    /* testDefaultArguments3() */
        {
            PsiElement psi = parseText("foo(a: S, x: I == 5)");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply foo (Decl a S) (Define (Decl x I) 5))", syntax.toString());
        }

    /* testSpadDeclare() */
        {
            PsiElement psi = parseSpadText("foo(a: S, b: T)");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply foo (SDecl a S) (SDecl b T))", syntax.toString());
        }

    /* testSpadExpr() */
        {
            PsiElement psi = parseSpadText("a + b");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Apply + a b)", syntax.toString());
        }


    /* testInfixedIdDecl() */
        {
            PsiElement psi = parseSpadText("\"+\": X");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(SDecl + X)", syntax.toString());
        }


    /* testZeroDecl() */
        {
            PsiElement psi = parseText("0: X");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(Decl 0 X)", syntax.toString());
        }

    /* testDecl() */
        {
            PsiElement psi = parseSpadText("f: (A, B) -> C");
            Syntax syntax = parse(psi);
            Assert.assertNotNull(syntax);
            Assert.assertEquals("(SDecl f (Apply -> (Comma A B) C))", syntax.toString());
        }
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(getProject(), text);
    }

    private PsiElement parseSpadText(CharSequence text) {
        return ParserFunctions.parseSpadText(getProject(), text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }


}
