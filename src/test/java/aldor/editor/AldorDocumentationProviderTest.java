package aldor.editor;

import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static java.util.Optional.ofNullable;

public class AldorDocumentationProviderTest {

    private static final Logger LOG = Logger.getInstance(AldorDocumentationProviderTest.class);
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();
    private final AnnotationFileTestFixture annotationTextFixture = new AnnotationFileTestFixture();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor());

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(JUnits.setLogToDebugTestRule)
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(annotationTextFixture.rule(codeTestFixture::getProject));

    @After
    public void tearDown() throws Exception {
        EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
    }

    public void xtestDocumentationLocal() throws Exception {
        String program = "#include \"aldor\"\n"
                + "+++ This is a domain\n"
                + "Dom: with == add;\n"
                + "f(x: Dom): Dom == x;\n";
        VirtualFile sourceFile = annotationTextFixture.createFile(codeTestFixture.getProject(),"foo.as", program);
        annotationTextFixture.createFile(codeTestFixture.getProject(),"Makefile",
                "out/ao/foo.ao: foo.as\n" +
                "\tmkdir -p out/ao\n" +
                "\t" + aldorExecutableRule.executable() + " -Fabn=out/ao/foo.abn -Fabn=out/ao/foo.abn foo.as\n");

        showProject("LOCAL");

        annotationTextFixture.compileFile(sourceFile, codeTestFixture.getProject());

        annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = ofNullable(PsiManager.getInstance(codeTestFixture.getProject()).findFile(sourceFile)).orElseThrow(() -> new AssertionFailedError(""));
            String docs = docForElement(PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("Dom)"), AldorIdentifier.class, true));
            LOG.info("Docs are: " + docs);
            Assert.assertNotNull(docs);
            Assert.assertTrue(docs.contains("This is a domain"));
        });

    }

    @Test
    public void testExportDocco() throws Exception {
        String program = "#include \"aldor\"\n"
                + "Dom: with { foo: () -> % } == add { foo(): % == never }\n"
                + "f(): Dom == foo();\n";
        VirtualFile sourceFile = annotationTextFixture.createFile(codeTestFixture.getProject(), "foo.as", program);
        annotationTextFixture.createFile(codeTestFixture.getProject(), "Makefile",
                "out/ao/foo.ao: foo.as\n" +
                    "\tmkdir -p out/ao\n" +
                "\t" + aldorExecutableRule.executable() + " -Fao=out/ao/foo.ao -Fabn=out/ao/foo.abn foo.as");
        showProject("EXPORT");
        annotationTextFixture.compileFile(sourceFile, codeTestFixture.getProject());

        annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = ofNullable(PsiManager.getInstance(codeTestFixture.getProject()).findFile(sourceFile)).orElseThrow(() -> new AssertionFailedError("Missing file!"));
            String docs = docForElement(PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("foo();"), AldorIdentifier.class, true));
            LOG.info("Docs are: " + docs);
            Assert.assertNotNull(docs);
            Assert.assertTrue("Doc is " + docs, docs.contains("<p><b>exporter: </b> Dom</b></p>"));
            Assert.assertTrue("Doc is " + docs, docs.contains("<p><b>type: </b> () -> Dom</p>"));
        });
    }

    private void showProject(String test) {

        Project project = codeTestFixture.getProject();
        VirtualFile[] roots = ProjectRootManager.getInstance(project).getContentRoots();
        LOG.info("TEST: Project " + project.getName() + " " + project.getBasePath());
        for (VirtualFile r: roots) {
            LOG.info("TEST: " + test + " Content Root: " + r);
        }
        roots = ProjectRootManager.getInstance(project).getContentSourceRoots();
        for (VirtualFile r: roots) {
            LOG.info("TEST: " + test + " Content Source Root: " + r);
        }
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module: modules) {
            ModuleRootManager.getInstance(module).getContentRoots();
            for (VirtualFile r: roots) {
                LOG.info("TEST: " + test + " Module: "+ module.getName() + ": Content Root: " + r);
            }
        }
    }


    @Test
    public void testImportDocco() throws Exception {
        String program = "#include \"aldor\"\n"
                + "import from Integer;\n"
                + "myabs := abs;\n";
        VirtualFile sourceFile = annotationTextFixture.createFile(codeTestFixture.getProject(), "bar.as", program);
        String sourceBaseName = annotationTextFixture.sourceDirectory(codeTestFixture.getProject()).getName();
        annotationTextFixture.createFile(codeTestFixture.getProject(), "Makefile",
                "out/ao/bar.ao: bar.as\n\tmkdir -p out/ao\n" +
                    "\t" + aldorExecutableRule.executable() + " -Fabn=out/ao/bar.abn -Fao=out/ao/bar.ao bar.as\n" +
                "out/jar/" + sourceBaseName + ".jar:\n" +
                "\tmkdir -p $(dir $@); touch $@\n");
        showProject("IMPORT");

        annotationTextFixture.compileFile(sourceFile, codeTestFixture.getProject());
        annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = ofNullable(PsiManager.getInstance(codeTestFixture.getProject()).findFile(sourceFile)).orElseThrow(() -> new AssertionFailedError("Missing file!"));
            String docs = docForElement(PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("abs;"), AldorIdentifier.class, true));
            LOG.info("Docs are: " + docs);
            Assert.assertNotNull(docs);
            Assert.assertTrue("Doc is " + docs, docs.contains("<b>type: </b> AldorInteger -> AldorInteger</p>"));
        });
    }


    private String docForElement(PsiElement id) {
        return DocumentationManager.getProviderFromElement(id).generateDoc(id, id);
    }

    protected LightProjectDescriptor getProjectDescriptor() {
       return SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorExecutableRule.prefix());
    }
}
