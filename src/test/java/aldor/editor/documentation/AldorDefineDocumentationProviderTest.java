package aldor.editor.documentation;

import aldor.parser.ParserFunctions;
import aldor.parser.SwingThreadTestRule;
import aldor.psi.AldorDefine;
import aldor.psi.elements.AldorTypes;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AldorDefineDocumentationProviderTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);
    private final AnnotationFileTestFixture annotationTestFixture = new AnnotationFileTestFixture();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(annotationTestFixture.rule(testFixture::getProject))
                    .around(new SwingThreadTestRule());

    @Test
    public void testOne() {
        String text = "++ This is documentation\nFoo: with == add";

        VirtualFile file = annotationTestFixture.createFile("foo.spad", text);
        PsiElement psi = PsiManager.getInstance(testFixture.getProject()).findFile(file);
        DefineDocumentationProvider docCreator = new DefineDocumentationProvider();
        String docs = docCreator.generateDoc(PsiTreeUtil.findChildOfType(psi, AldorDefine.class), null);
        assertNotNull(docs);
        assertTrue(docs.contains("External link"));
        assertTrue(docs.contains("Foo.html"));
        assertTrue(docs.contains("This is documentation"));
    }

    private PsiElement parseSpadText(CharSequence text) {
        return ParserFunctions.parseSpadText(testFixture.getProject(), text, AldorTypes.TOP_LEVEL);
    }

}
