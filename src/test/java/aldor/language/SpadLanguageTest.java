package aldor.language;

import aldor.parser.ParserFunctions;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

public class SpadLanguageTest extends BasePlatformTestCase {

    public void testFoo() {
        PsiFile file = createSpadFile(")abbrev category A Aa\n++ Aa is great\n" +
                "Aa: Category ==  B\n" +
                ")abbrev category B Bb\n++Bb is good too\n" +
                "Bb: Category == C\n");

        Assert.assertEquals(0, ParserFunctions.getPsiErrorElements(file).size());
    }


    private PsiFile createSpadFile(String text) {
        return createLightFile("foo.spad", SpadLanguage.INSTANCE, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR;
    }

}
