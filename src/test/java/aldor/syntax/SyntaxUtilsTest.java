package aldor.syntax;

import aldor.parser.ParserFunctions;
import aldor.parser.SwingThreadTestRule;
import aldor.psi.elements.AldorTypes;
import aldor.syntax.components.Id;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;

public class SyntaxUtilsTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new SwingThreadTestRule());

    @Test
    public void testTypeNameId() {
        Syntax syntax = Id.createImplicitId("A");
        assertEquals("A", SyntaxPrinter.instance().toString(SyntaxUtils.typeName(syntax)));
    }

    @Test
    public void testApply() {
        Syntax syntax = parseToSyntax("Foo(n: Integer): Y");
        Syntax name = SyntaxUtils.typeName(syntax);
        assertEquals("Foo n", SyntaxPrinter.instance().toString(name));
    }

    @Test
    public void testApply2() {
        Syntax syntax = parseToSyntax("Foo(n: Integer, M: Ring): Y");
        Syntax name = SyntaxUtils.typeName(syntax);
        assertEquals("Foo(n, M)", SyntaxPrinter.instance().toString(name));
    }


    private Syntax parseToSyntax(CharSequence text) {
        PsiElement element = parseText(text);
        return SyntaxPsiParser.parse(element);
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(testFixture.getProject(), text, AldorTypes.TOP_LEVEL);
    }

}
