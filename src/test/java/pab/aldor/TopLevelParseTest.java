package pab.aldor;

import aldor.AldorTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;

import java.util.List;

import static pab.aldor.ParserFunctions.getPsiErrorElements;

public class TopLevelParseTest extends LightPlatformCodeInsightTestCase {

    public void testTopLevelStd() {
        PsiElement psi = parseText("Foo: with == add\nQQQ: Category == with\n");
        final List<PsiErrorElement> errors = getPsiErrorElements(psi);
        assertEquals(0, errors.size());
    }

    @SuppressWarnings("SameParameterValue")
    private PsiElement parseText(CharSequence text) {
        return parseText(text, AldorTypes.TOP_LEVEL);
    }

    @SuppressWarnings("SameParameterValue")
    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseText(getProject(), text, elementType);
    }


}
