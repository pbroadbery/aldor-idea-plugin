package aldor.syntax;

import aldor.parser.ParserFunctions;
import aldor.test_util.AssumptionAware;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.io.PrintWriter;
import java.io.StringWriter;

import static aldor.syntax.SyntaxPsiParser.parse;

public class SyntaxPrintTest extends AssumptionAware.BasePlatformTestCase {

    public void testApplyId() {
        Assert.assertEquals("f", parseAndPrint("f"));
        Assert.assertEquals("f()", parseAndPrint("f()"));
        Assert.assertEquals("f x", parseAndPrint("f x"));
        Assert.assertEquals("f(x, y)", parseAndPrint("f(x, y)"));
        Assert.assertEquals("f f x", parseAndPrint("f f x"));
        Assert.assertEquals("(f a) x", parseAndPrint("(f a) x"));
        Assert.assertEquals("foo(x: Int)", parseAndPrint("foo(x: Int)"));
        Assert.assertEquals("'a, b'", parseAndPrint("'a, b'"));
    }

    public void testApplyMap() {
        Assert.assertEquals("A -> B", parseAndPrint("A -> B"));
        Assert.assertEquals("() -> Dom", parseAndPrint("() -> Dom"));
    }

    private String parseAndPrint(CharSequence text) {
        PsiElement psi = parseText(text);
        Syntax syntax = parse(psi);
        Assert.assertNotNull(syntax);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        SyntaxPrinter.instance().print(pw, syntax);
        return sw.toString();
    }


    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(getProject(), text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }


}
