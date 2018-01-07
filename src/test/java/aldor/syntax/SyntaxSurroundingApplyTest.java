package aldor.syntax;

import aldor.parser.ParserFunctions;
import aldor.psi.AldorId;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

import static aldor.syntax.SyntaxPsiParser.SurroundType.Any;
import static aldor.syntax.SyntaxPsiParser.SurroundType.Leading;

public class SyntaxSurroundingApplyTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testFirstPosn() {
        PsiElement psi = parseText("List X");

        Syntax application = SyntaxPsiParser.surroundingApplication(PsiTreeUtil.findChildOfType(psi, AldorId.class), Any);
        Assert.assertNotNull(application);
        Assert.assertEquals("(Apply List X)", application.toString());
        System.out.println("Application: " + application);
    }

    public void testId1() {
        PsiElement psi = parseText("X");

        Syntax application = SyntaxPsiParser.surroundingApplication(PsiTreeUtil.findChildOfType(psi, AldorId.class), Any);
        Assert.assertNotNull(application);
        Assert.assertEquals("X", application.toString());
        System.out.println("Application: " + application);
    }

    public void testSurroundingApplicationOne() {
        // handy to test a single case
        checkSurroundingApplication("F(A, B)", "A", Any, "(Apply F A B)");
    }

    public void testSurroundingApplication() {
        checkSurroundingApplication("F X", "X", Any, "(Apply F X)");
        checkSurroundingApplication("F X", "X", Leading, "X");

        checkSurroundingApplication("List Integer", "List", Any, "(Apply List Integer)");
        checkSurroundingApplication("List Integer", "List", Leading, "(Apply List Integer)");

        checkSurroundingApplication("F G X", "G", Any, "(Apply G X)");
        checkSurroundingApplication("F G X", "G", Leading, "(Apply G X)");

        checkSurroundingApplication("F(A, B)", "F", Leading, "(Apply F A B)");
        checkSurroundingApplication("F(A, B)", "F", Any, "(Apply F A B)");
        checkSurroundingApplication("F(A, B)", "A", Any, "(Apply F A B)");
        checkSurroundingApplication("F(A, B)", "A", Leading, "A");
    }

    private void checkSurroundingApplication(CharSequence text, String search, SyntaxPsiParser.SurroundType type, String expected) {
        PsiElement psi = parseText(text);

        Syntax application = SyntaxPsiParser.surroundingApplication(psi.findElementAt(psi.getText().indexOf(search)), type);
        Assert.assertNotNull(application);
        Assert.assertEquals(expected, application.toString());
        System.out.println("Application: " + text + " - " + search +" " + type + " = " + application);
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
