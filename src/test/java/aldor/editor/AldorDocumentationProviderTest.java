package aldor.editor;

import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
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
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import junit.framework.AssertionFailedError;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.io.File;

import static java.util.Optional.ofNullable;

public class AldorDocumentationProviderTest extends LightPlatformCodeInsightFixtureTestCase {

    private static final Logger LOG = Logger.getInstance(AldorDocumentationProviderTest.class);
    private final AnnotationFileTestFixture annotationTextFixture = new AnnotationFileTestFixture();

    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();

    @Override
    protected boolean shouldRunTest() {
        return super.shouldRunTest() && aldorExecutableRule.shouldRunTest();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
        }
        finally {
            super.tearDown();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnits.setLogToDebug();
        if ((getProject().getWorkspaceFile() != null) && (getProject().getWorkspaceFile().getCanonicalPath() != null)) {
            this.addTmpFileToKeep(new File(getProject().getWorkspaceFile().getCanonicalPath()));
        }
        Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        for (Module module: modules) {
            if ((module.getModuleFile() != null) && (module.getModuleFile().getCanonicalPath() != null)) {
                this.addTmpFileToKeep(new File(module.getModuleFile().getCanonicalPath()));
            }
        }
        JUnits.enableJpsDebugging(false);
    }

    public void testDocumentationLocal() throws Exception {
        showProject("LOCAL");
        String program = "#include \"aldor\"\n"
                + "+++ This is a domain\n"
                + "Dom: with == add;\n"
                + "f(x: Dom): Dom == x;\n";
        VirtualFile sourceFile = annotationTextFixture.createFile(getProject(),"foo.as", program);
        annotationTextFixture.createFile(getProject(),"Makefile",
                "out/ao/foo.ao: foo.as\n" +
                "\tmkdir -p out/ao\n" +
                "\t" + aldorExecutableRule.executable() + " -Fabn=out/ao/foo.abn -Fabn=out/ao/foo.abn foo.as\n");

        annotationTextFixture.compileFile(sourceFile, getProject());

        annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = ofNullable(PsiManager.getInstance(getProject()).findFile(sourceFile)).orElseThrow(() -> new AssertionFailedError(""));
            String docs = docForElement(PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("Dom)"), AldorIdentifier.class, true));
            LOG.info("Docs are: " + docs);
            Assert.assertNotNull(docs);
            Assert.assertTrue(docs.contains("This is a domain"));
        });

    }

    public void testExportDocco() throws Exception {
        showProject("EXPORT");
        String program = "#include \"aldor\"\n"
                + "Dom: with { foo: () -> % } == add { foo(): % == never }\n"
                + "f(): Dom == foo();\n";
        VirtualFile sourceFile = annotationTextFixture.createFile(getProject(), "foo.as", program);
        annotationTextFixture.createFile(getProject(), "Makefile",
                "out/ao/foo.ao: foo.as\n" +
                    "\tmkdir -p out/ao\n" +
                "\t" + aldorExecutableRule.executable() + " -Fao=out/ao/foo.ao -Fabn=out/ao/foo.abn foo.as");

        annotationTextFixture.compileFile(sourceFile, getProject());

        annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = ofNullable(PsiManager.getInstance(getProject()).findFile(sourceFile)).orElseThrow(() -> new AssertionFailedError("Missing file!"));
            String docs = docForElement(PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("foo();"), AldorIdentifier.class, true));
            LOG.info("Docs are: " + docs);
            Assert.assertNotNull(docs);
            Assert.assertTrue("Doc is " + docs, docs.contains("<p><b>exporter: </b> Dom</b></p>"));
            Assert.assertTrue("Doc is " + docs, docs.contains("<p><b>type: </b> () -> Dom</p>"));
        });
    }

    private void showProject(String test) {

        Project project = this.getProject();
        VirtualFile[] roots = ProjectRootManager.getInstance(project).getContentRoots();
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


    public void testImportDocco() throws Exception {
        showProject("IMPORT");
        String program = "#include \"aldor\"\n"
                + "import from Integer;\n"
                + "myabs := abs;\n";
        VirtualFile sourceFile = annotationTextFixture.createFile(getProject(), "bar.as", program);
        String sourceBaseName = annotationTextFixture.sourceDirectory(getProject()).getName();
        annotationTextFixture.createFile(getProject(), "Makefile",
                "out/ao/bar.ao: bar.as\n\tmkdir -p out/ao\n" +
                    "\t" + aldorExecutableRule.executable() + " -Fabn=out/ao/bar.abn -Fao=out/ao/bar.ao bar.as\n" +
                "out/jar/" + sourceBaseName + ".jar:\n" +
                "\tmkdir -p $(dir $@); touch $@\n");

        annotationTextFixture.compileFile(sourceFile, getProject());
        annotationTextFixture.runInEdtAndWait(() -> {
            PsiFile psiFile = ofNullable(PsiManager.getInstance(getProject()).findFile(sourceFile)).orElseThrow(() -> new AssertionFailedError("Missing file!"));
            String docs = docForElement(PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("abs;"), AldorIdentifier.class, true));
            LOG.info("Docs are: " + docs);
            Assert.assertNotNull(docs);
            Assert.assertTrue("Doc is " + docs, docs.contains("<b>type: </b> AldorInteger -> AldorInteger</p>"));
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
