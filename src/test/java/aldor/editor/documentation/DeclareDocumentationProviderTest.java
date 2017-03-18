package aldor.editor.documentation;

import aldor.parser.ParserFunctions;
import aldor.parser.SwingThreadTestRule;
import aldor.psi.AldorDeclare;
import aldor.psi.elements.AldorTypes;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeclareDocumentationProviderTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);
    private final AnnotationFileTestFixture annotationTestFixture = new AnnotationFileTestFixture();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(annotationTestFixture.rule(testFixture::getProject))
                    .around(new SwingThreadTestRule());

    @Test
    public void testDomainExport() {
        String text = "++ This is documentation\nFoo: with { x: String } == add";

        VirtualFile file = annotationTestFixture.createFile("foo.spad", text);
        PsiFile psi = PsiManager.getInstance(testFixture.getProject()).findFile(file);
        DeclareDocumentationProvider docCreator = new DeclareDocumentationProvider();
        assert psi != null;
        String docs = docCreator.generateDoc(PsiTreeUtil.findElementOfClassAtOffset(psi, text.indexOf("x: "), AldorDeclare.class, true), null);
        assertNotNull(docs);
        System.out.println("Docs are: " + docs);
        assertTrue(docs.contains("String"));
        assertTrue(docs.contains("Exporter"));
        assertTrue(docs.contains("Foo"));
    }

    @Test
    public void testCategoryExport() {
        String text = "++ This is documentation\nFoo: Category == Join(X,Y) with { x: String }";

        VirtualFile file = annotationTestFixture.createFile("foo.spad", text);
        PsiFile psi = PsiManager.getInstance(testFixture.getProject()).findFile(file);
        DeclareDocumentationProvider docCreator = new DeclareDocumentationProvider();
        assert psi != null;
        String docs = docCreator.generateDoc(PsiTreeUtil.findElementOfClassAtOffset(psi, text.indexOf("x: "), AldorDeclare.class, true), null);
        assertNotNull(docs);
        System.out.println("Docs are: " + docs);
        assertTrue(docs.contains("String"));
        assertTrue(docs.contains("Exporter"));
        assertTrue(docs.contains("Foo"));
    }


    private PsiElement parseSpadText(CharSequence text) {
        return ParserFunctions.parseSpadText(testFixture.getProject(), text, AldorTypes.TOP_LEVEL);
    }

}
