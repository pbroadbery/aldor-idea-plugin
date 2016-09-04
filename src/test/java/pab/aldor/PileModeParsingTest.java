package pab.aldor;

import aldor.AldorTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;

import java.util.List;

import static pab.aldor.ParserFunctions.getPsiErrorElements;
import static pab.aldor.ParserFunctions.logPsi;

public class PileModeParsingTest extends LightPlatformCodeInsightTestCase{


    public void testPileMode() {
        String text = "#pile\nrepeat\n  foo\n  bar";
        PsiElement psi = parseText(text, AldorTypes.BAL_STATEMENT);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }


    public void testPileAfter2Eq() {
        String text = "#pile\nfoo: X == \n 1\n 2";
        PsiElement psi = parseText(text, AldorTypes.COMMA);
        logPsi(psi, 0);
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseText(getProject(), text, elementType);
    }

}
