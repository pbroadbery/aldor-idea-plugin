package aldor.symbolfile;

import aldor.annotations.AnnotationFileManager;
import aldor.annotations.AnnotationFileNavigator;
import aldor.annotations.DefaultAnnotationFileNavigator;
import aldor.psi.AldorIdentifier;
import aldor.syntax.SyntaxPrinter;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
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
    public void tearDown() throws Exception {
        try {
            EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
        }
        finally {
            super.tearDown();
        }
    }

    @Override
    protected boolean shouldRunTest() {
        return super.shouldRunTest() && aldorExecutableRule.shouldRunTest();
    }

    public void testFullRoundTrip() throws Exception {
        Project project = getProject();

        annotationTextFixture.createFile(getProject(), "Makefile", "out/ao/foo.ao: foo.as\n" +
                "\tmkdir -p out/ao\n" +
                "\t" + aldorExecutableRule.executable() + " -Fabn=out/ao/foo.abn -Fao=out/ao/foo.ao foo.as");
        VirtualFile sourceFile = annotationTextFixture.createFile(getProject(), "foo.as", "#include \"aldor\"\nfoo(n: Integer): Integer == n+" + System.currentTimeMillis());

        annotationTextFixture.compileFile(sourceFile, getProject());

       annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(sourceFile);
            AnnotationFileManager annotationManager = AnnotationFileManager.getAnnotationFileManager(project);
            Assert.assertNotNull(psiFile);

            AnnotationFile annotationFile = annotationManager.annotationFile(psiFile);
            AnnotationFileNavigator navigator = new DefaultAnnotationFileNavigator(annotationManager);
            Assert.assertNull(annotationFile.errorMessage());
            Collection<AldorIdentifier> elts = PsiTreeUtil.findChildrenOfType(psiFile, AldorIdentifier.class);
            List<AldorIdentifier> nInstances = elts.stream().filter(id -> "n".equals(id.getText())).collect(Collectors.toList());
            Assert.assertFalse(nInstances.isEmpty());
            Assert.assertTrue(nInstances.stream()
                                        .map(navigator::findSrcPosForElement)
                                        .map(annotationFile::lookupSyme)
                    .allMatch(symes -> {
                                    if (symes.isEmpty()) {
                                        return false;
                                    }
                                    Syme syme = symes.stream().filter(s -> "n".equals(s.name())).findFirst().orElse(null);
                                    Assert.assertNotNull(syme);
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
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorExecutableRule.prefix());
    }

}
