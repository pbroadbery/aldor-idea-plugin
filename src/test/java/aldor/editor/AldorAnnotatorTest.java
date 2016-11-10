package aldor.editor;

import aldor.language.AldorLanguage;
import aldor.parser.EnsureParsingTest;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static aldor.psi.AldorPsiUtils.logPsi;

public class AldorAnnotatorTest extends LightPlatformCodeInsightFixtureTestCase {


    public void testAnnotations() {
        String text = "f(x: String): Integer == x::Integer";
        PsiFile file = createAldorFile(text);
        logPsi(file);

        //FileViewProvider vp = getPsiManager().findViewProvider(file.getVirtualFile());

    }


    private PsiFile createAldorFile(String text) {
        return createLightFile("foo.as", AldorLanguage.INSTANCE, text);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new EnsureParsingTest.AldorProjectDescriptor();
    }

}
