package aldor.runconfiguration.aldor;

import aldor.build.builders.AldorJarOnlyScope;
import aldor.psi.AldorWith;
import aldor.runconfiguration.MyMapDataContext;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.SdkProjectDescriptors;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Assert;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static aldor.test_util.JUnits.JpsDebuggingState.OFF;
import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;

public class AldorUnitRunnableStateTest extends AssumptionAware.BasePlatformTestCase {
    private static final Logger LOG = Logger.getInstance(AldorUnitRunnableStateTest.class);

    private final AnnotationFileTestFixture annotationFileTestFixture = new AnnotationFileTestFixture();
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.AldorDev();
    private boolean saveAllowed;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        JUnits.setLogToDebug();
        JUnits.enableJpsDebugging(OFF);
        saveAllowed = ApplicationManagerEx.getApplicationEx().isSaveAllowed();
        ApplicationManagerEx.getApplicationEx().setSaveAllowed(true);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
        }
        finally {
            ApplicationManagerEx.getApplicationEx().setSaveAllowed(saveAllowed);
            super.tearDown();
        }
    }

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }

    @Override
    protected void invokeTestRunnable(Runnable runnable) {
        runnable.run();
    }

    @Override
    protected boolean shouldRunTest() {
        return super.shouldRunTest() && aldorExecutableRule.shouldRunTest();
    }

    public void testRunSimpleConfiguration() throws InterruptedException {
        Ref<PsiElement> eltRef = new Ref<>();
        //getProject().getProjectFile().refresh(false, false);
        EdtTestUtil.runInEdtAndWait(() -> {
            VirtualFile file = createFile(getSourceRoot(), "foo.as",
                    "#include \"aldor.as\"\n" +
                            "#pile\n" +
                            "ALDORUNIT__RUNNER ==> annotation==org_.junit_.runner_.RunWith(aldor_.aldorunit_.runner_.AldorUnitRunner.class)\n" +
                            "ALDORUNIT__SOURCEFILE ==> annotation==aldor_.aldorunit_.SourceFile()\n" +
                            "\n" +
                            "export FooTest to Foreign Java(\"aldor.test\")\n" +
                            "FooTest: with\n" +
                            "    test: () -> ()\n" +
                            "== add \n" +
                            "    test(): () == return\n" +
                            "\n");

            String makefileText = annotationFileTestFixture.makefileBuilder(aldorExecutableRule.executable(), Collections.singleton("foo.as"))
                    .withSourceDirectory(getSourceRoot())
                    .withProject(getProject())
                    .withAldorUnit()
                    .withJarRule()
                    .build();
            LOG.info("Makefile: \n" + makefileText);
            createFile(getSourceRoot(), "Makefile", makefileText);
            PsiFile whole = PsiManager.getInstance(getProject()).findFile(file);
            Assert.assertNotNull(whole);
            AldorWith elt = PsiTreeUtil.findChildOfType(whole, AldorWith.class);
            Assert.assertNotNull(elt);
            eltRef.set(elt);
        });

        CompilerManager compilerManager = CompilerManager.getInstance(getProject());
        CountDownLatch latch = new CountDownLatch(1);
        Ref<Boolean> okCompile = new Ref<>(false);
        EdtTestUtil.runInEdtAndWait(() -> compilerManager.compile(new AldorJarOnlyScope(this.getModule(), null), (aborted, errors, warnings, compileContext) -> {
            LOG.info("Finished: " + (aborted ? "failed" : "ok") + " errors " + errors + " warnings: " + warnings);
            okCompile.set(!aborted && (errors == 0));
            latch.countDown();
        }));
        latch.await();
        Assert.assertTrue(okCompile.get());
        EdtTestUtil.runInEdtAndWait(() -> {
            MyMapDataContext dataContext = new MyMapDataContext();
            dataContext.put("module", getModule());
            dataContext.put("Location", new PsiLocation<>(eltRef.get()));
            dataContext.put("project", getProject());

            ConfigurationContext runContext = ConfigurationContext.getFromContext(dataContext);
            LOG.info("Context: " + runContext.getLocation() + " " + runContext.getConfiguration());
            Assert.assertNotNull(runContext.getConfiguration());
            RunnerAndConfigurationSettings runnerAndConfigurationSettings = runContext.getConfiguration();

            JUnits.ProcessOutput output = JUnits.doStartTestsProcess(runnerAndConfigurationSettings.getConfiguration());
            LOG.info("Sys: " + output.sys);
            LOG.info("Out: " + output.out);
            LOG.info("Err: " + output.err);
            Assert.assertEquals(0, output.exitCode);
        });
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptorWithAldorUnit(aldorExecutableRule.prefix());
    }

}