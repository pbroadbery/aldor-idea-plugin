package aldor.expression;

import aldor.parser.ParserFunctions;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;

import java.util.List;

import static aldor.psi.AldorPsiUtils.logPsi;

public class ExpressionTest extends LightPlatformCodeInsightTestCase {

    //{setLogToDebug();}

    public void testOne() {
        PsiElement element = parseExpressionText(getProject(), "a * b + c * d");
        assertNotNull(element);
        logPsi(element);
    }

    public void testOneA() {
        PsiElement element = parseExpressionText(getProject(), "a + b * c + d");
        assertNotNull(element);
        logPsi(element);
    }

    public void testTwo() {
        PsiElement element = parseExpressionText(getProject(), "(a: % + b: %): %");
        assertNotNull(element);
        logPsi(element);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(element);
        assertEquals(0, errors.size());
    }

    public void testFnCall() {
        PsiElement element = parseExpressionText(getProject(), "f x.y (a + b * c)");
        assertNotNull(element);
        logPsi(element);
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(element);
        assertEquals(0, errors.size());
    }


    public static PsiElement parseExpressionText(Project project, CharSequence text) {
        return parseExpressionText(project, text, ExpressionTypes.SIMPLE_EXPRESSION);
    }

    public static PsiElement parseExpressionText(Project project, CharSequence text, IElementType elementType) {
        ParserDefinition expressionParserDefinition = new ExpressionParserDefinition();
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(expressionParserDefinition, expressionParserDefinition.createLexer(null),
                text);

        PsiParser parser = expressionParserDefinition.createParser(project);
        ASTNode parsed = parser.parse(elementType, psiBuilder);

        return parsed.getPsi();
    }

}
