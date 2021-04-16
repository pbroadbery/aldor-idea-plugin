package aldor.language;

import aldor.parser.ParserFunctions;
import aldor.psi.AldorPsiUtils;
import aldor.test_util.AssumptionAware;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.stream.Collectors;

public class SpadLanguageTest extends AssumptionAware.BasePlatformTestCase {

    public void testFoo() {
        PsiFile file = createSpadFile("\n)abbrev category A Aa\n" +
                "++ Aa is great\n" +
                "Aa: Category == B\n" +
                "\n" +
                ")abbrev category B Bb\n" +
                "++Bb is good too\n" +
                "Bb: Category == C\n");

        System.out.println(ParserFunctions.getPsiErrorElements(file).stream().map(x -> x.getText()).collect(Collectors.joining()));
        AldorPsiUtils.logPsi(file);
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
