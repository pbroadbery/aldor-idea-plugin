package aldor.editor.documentation;

import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.Htmls;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IdentifierDocumentProviderTest {

    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorExecutableRule.prefix()));
    private final AnnotationFileTestFixture annotationTestFixture= new AnnotationFileTestFixture();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(annotationTestFixture.rule(codeTestFixture::getProject));

    @After
    public void doAfter() {
        EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);

    }


    @Test
    public void testExporterIdentifierDocumentation() throws ExecutionException, InterruptedException {
        String makefileText = annotationTestFixture.createMakefile(aldorExecutableRule.executable().getAbsolutePath(), Collections.singleton("foo.as"));
        VirtualFile makefileFile = annotationTestFixture.createFile(getProject(), "Makefile", makefileText);
        String program = "#include \"aldor\"\n" +
                "Foo(X: with): with { id: % -> %} == add { id(x: %): % == x }\n" +
                "fn(x: Foo String): Foo String == id x\n";
        VirtualFile testFile = annotationTestFixture.createFile(getProject(), "foo.as", program);

        annotationTestFixture.compileFile(testFile, getProject());

        EdtTestUtil.runInEdtAndWait(() -> {
            IdentifierDocumentationProvider docProvider = new IdentifierDocumentationProvider();
            PsiFile testPsiFile = PsiManager.getInstance(codeTestFixture.getProject()).findFile(testFile);
            assertNotNull(testPsiFile);
            AldorIdentifier idCall = PsiTreeUtil.findElementOfClassAtOffset(testPsiFile, program.indexOf("id x"), AldorIdentifier.class, true);
            String text = docProvider.getQuickNavigateInfo(idCall, idCall);
            assertNotNull(text);
            assertTrue(Htmls.removeTags(text).contains("from Foo String"));
        });
    }

    @Test
    public void testIdentifierDocumentation() throws ExecutionException, InterruptedException {
        String makefileText = annotationTestFixture.createMakefile(aldorExecutableRule.executable().getAbsolutePath(), Collections.singleton("foo.as"));
        VirtualFile makefileFile = annotationTestFixture.createFile(getProject(), "Makefile", makefileText);
        String program = "#include \"aldor\"\n" +
                "Foo(X: with): with { id: % -> %} == add { id(x: %): % == id x }\n";
        VirtualFile testFile = annotationTestFixture.createFile(getProject(), "foo.as", program);

        annotationTestFixture.compileFile(testFile, getProject());

        EdtTestUtil.runInEdtAndWait(() -> {
            IdentifierDocumentationProvider docProvider = new IdentifierDocumentationProvider();
            PsiFile testPsiFile = PsiManager.getInstance(codeTestFixture.getProject()).findFile(testFile);
            assertNotNull(testPsiFile);
            AldorIdentifier idCall = PsiTreeUtil.findElementOfClassAtOffset(testPsiFile, program.indexOf("id x"), AldorIdentifier.class, true);
            String text = docProvider.getQuickNavigateInfo(idCall, idCall);
            assertNotNull(text);
            text = Htmls.removeTags(text);
            assertTrue(text.contains("id: (x: %) -> %"));
            assertFalse(text.contains("from"));
        });
    }


    private Project getProject() {
        return codeTestFixture.getProject();
    }

}
