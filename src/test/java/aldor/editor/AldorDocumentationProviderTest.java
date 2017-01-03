package aldor.editor;

import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AldorRoundTripProjectDescriptor;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import junit.framework.AssertionFailedError;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Rule;

import static java.util.Optional.ofNullable;

public class AldorDocumentationProviderTest extends LightPlatformCodeInsightFixtureTestCase {

    private static final Logger LOG = Logger.getInstance(AldorDocumentationProviderTest.class);
    private final AnnotationFileTestFixture annotationTextFixture = new AnnotationFileTestFixture();
    @Rule
    public final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();

    @Override
    protected boolean shouldRunTest() {
        return super.shouldRunTest() && aldorExecutableRule.shouldRunTest();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnits.setLogToDebug();
    }

    public void testDocumentationLocal() throws Exception {
        annotationTextFixture.project(getProject());

        String program = "#include \"aldor\"\n"
                + "+++ This is a domain\n"
                + "Dom: with == add;\n"
                + "f(x: Dom): Dom == x;\n";
        VirtualFile sourceFile = annotationTextFixture.createFile("foo.as", program);
        annotationTextFixture.createFile("Makefile", "foo.abn: foo.as\n\t" + aldorExecutableRule.executable() + " -Fabn=foo.abn foo.as");

        annotationTextFixture.compileFile(sourceFile);

        annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = ofNullable(PsiManager.getInstance(getProject()).findFile(sourceFile)).orElseThrow(() -> new AssertionFailedError(""));
            String docs = docForElement(PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("Dom)"), AldorIdentifier.class, true));
            LOG.info("Docs are: " + docs);
            Assert.assertNotNull(docs);
            Assert.assertTrue(docs.contains("This is a domain"));
        });

    }

    public void testExportDocco() throws Exception {
        annotationTextFixture.project(getProject());

        String program = "#include \"aldor\"\n"
                + "Dom: with { foo: () -> % } == add { foo(): % == never }\n"
                + "f(): Dom == foo();\n";
        VirtualFile sourceFile = annotationTextFixture.createFile("foo.as", program);
        annotationTextFixture.createFile("Makefile", "foo.abn: foo.as\n\t" + aldorExecutableRule.executable() + " -Fabn=foo.abn foo.as");

        annotationTextFixture.compileFile(sourceFile);

        annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = ofNullable(PsiManager.getInstance(getProject()).findFile(sourceFile)).orElseThrow(() -> new AssertionFailedError("Missing file!"));
            String docs = docForElement(PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("foo();"), AldorIdentifier.class, true));
            LOG.info("Docs are: " + docs);
            Assert.assertNotNull(docs);
            Assert.assertTrue("Doc is " + docs, docs.contains("<p><b>exporter:</b> Dom</b></p>"));
            Assert.assertTrue("Doc is " + docs, docs.contains("<p><b>type:</b> () -> Dom</p>"));
        });
    }


    public void testImportDocco() throws Exception {
        annotationTextFixture.project(getProject());

        String program = "#include \"aldor\"\n"
                + "import from Integer;\n"
                + "myabs := abs;\n";
        VirtualFile sourceFile = annotationTextFixture.createFile("foo.as", program);
        annotationTextFixture.createFile("Makefile", "foo.abn: foo.as\n\t" + aldorExecutableRule.executable() + " -Fabn=foo.abn foo.as");

        annotationTextFixture.compileFile(sourceFile);

        annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = ofNullable(PsiManager.getInstance(getProject()).findFile(sourceFile)).orElseThrow(() -> new AssertionFailedError("Missing file!"));
            String docs = docForElement(PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("abs;"), AldorIdentifier.class, true));
            LOG.info("Docs are: " + docs);
            Assert.assertNotNull(docs);
            Assert.assertTrue("Doc is " + docs, docs.contains("<b>type:</b> AldorInteger -> AldorInteger</p>"));
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
