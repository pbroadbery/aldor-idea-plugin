package aldor.spad;

import aldor.EnsureParsingTest;
import aldor.ParserFunctions;
import aldor.language.SpadLanguage;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static aldor.AldorPsiUtils.logPsi;

public class SpadLanguageTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testFoo() {
        PsiFile file = createSpadFile(")abbrev category A Aa\n++ Aa is great\n" +
                "Aa: Category ==  B\n" +
                ")abbrev category B Bb\n++Bb is good too\n" +
                "Bb: Category == C\n");
        logPsi(file);

        assertEquals(0, ParserFunctions.getPsiErrorElements(file).size());
    }


    private PsiFile createSpadFile(String text) {
        return createLightFile("foo.spad", SpadLanguage.INSTANCE, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }

}
