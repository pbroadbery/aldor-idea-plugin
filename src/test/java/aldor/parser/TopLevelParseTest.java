package aldor.parser;

import aldor.psi.elements.AldorTypes;
import aldor.test_util.AssumptionAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import org.junit.Assert;

import java.util.List;

public class TopLevelParseTest extends AssumptionAware.LightPlatformCodeInsightTestCase {

    public void testTopLevelStd() {
        PsiElement psi = parseText("Foo: with == add\nQQQ: Category == with\n");
        final List<PsiErrorElement> errors = ParserFunctions.getPsiErrorElements(psi);
        Assert.assertEquals(0, errors.size());
    }

    private PsiElement parseText(CharSequence text) {
        return parseText(text, AldorTypes.TOP_LEVEL);
    }

    private PsiElement parseText(CharSequence text, IElementType elementType) {
        return ParserFunctions.parseAldorText(getProject(), text, elementType);
    }


}
