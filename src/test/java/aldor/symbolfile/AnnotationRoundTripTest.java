package aldor.symbolfile;

import aldor.build.module.AnnotationFileManager;
import aldor.psi.AldorIdentifier;
import aldor.syntax.SyntaxPrinter;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Rule;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationRoundTripTest extends LightPlatformCodeInsightFixtureTestCase {
    private static final Logger LOG = Logger.getInstance(AnnotationRoundTripTest.class);
    private final AnnotationFileTestFixture annotationTextFixture = new AnnotationFileTestFixture();
    @Rule
    public final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnits.setLogToDebug();
    }

    @Override
    protected boolean shouldRunTest() {
        return super.shouldRunTest() && aldorExecutableRule.shouldRunTest();
    }

    public void testFullRoundTrip() throws Exception {
        annotationTextFixture.project(getProject());
        Project project = getProject();

        annotationTextFixture.createFile("Makefile", "foo.abn: foo.as\n\t" + aldorExecutableRule.executable() + " -Fabn=foo.abn foo.as");
        VirtualFile sourceFile = annotationTextFixture.createFile("foo.as", "#include \"aldor\"\nfoo(n: Integer): Integer == n+" + System.currentTimeMillis());

        annotationTextFixture.compileFile(sourceFile);

       annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(sourceFile);
            AnnotationFileManager annotationManager = AnnotationFileManager.getAnnotationFileManager(project);
            Assert.assertNotNull(psiFile);
            AnnotationFile annotationFile = annotationManager.annotationFile(psiFile);
            Assert.assertNull(annotationFile.errorMessage());
            Collection<AldorIdentifier> elts = PsiTreeUtil.findChildrenOfType(psiFile, AldorIdentifier.class);
            List<AldorIdentifier> nInstances = elts.stream().filter(id -> "n".equals(id.getText())).collect(Collectors.toList());
            Assert.assertFalse(nInstances.isEmpty());
            Assert.assertTrue(nInstances.stream()
                                        .map(annotationManager::findSrcPosForElement)
                                        .map(annotationFile::lookupSyme)
                    .allMatch(syme -> {
                                    if (syme == null) {
                                        return false;
                                    }
                                    String pretty = SyntaxPrinter.instance().toString(syme.type());
                                    return "AldorInteger".equals(pretty);
                    }));

        });
    }

    private String docForElement(PsiElement id) {
        return DocumentationManager.getProviderFromElement(id).generateDoc(id, id);
    }

    @Override
    protected void invokeTestRunnable(@NotNull Runnable runnable) throws Exception {
        runnable.run();
    }

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }


    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new AldorRoundTripProjectDescriptor();
    }

}
