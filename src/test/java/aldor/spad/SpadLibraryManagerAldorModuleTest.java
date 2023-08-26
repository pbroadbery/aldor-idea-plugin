package aldor.spad;

import aldor.build.AldorCompilationService;
import aldor.file.AldorFileType;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.syntax.Syntax;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import aldor.test_util.SourceFileStorageType;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SpadLibraryManagerAldorModuleTest {
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();
    private final CodeInsightTestFixture codeTestFixture = createFixture(SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorExecutableRule, SourceFileStorageType.Real));
    private final AnnotationFileTestFixture annotationTestFixture= new AnnotationFileTestFixture();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(annotationTestFixture.rule(codeTestFixture::getProject))
                    .around(JUnits.prePostTestRule(() -> {
                        ApplicationManagerEx.getApplicationEx().setSaveAllowed(true);
                        getProject().save();
                    }, () -> ApplicationManagerEx.getApplicationEx().setSaveAllowed(false)));

    @After
    public void doAfter() {
        EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
    }

    @Test
    public void testModule() throws ExecutionException, InterruptedException {
        JUnits.setLogToInfo();

        AldorCompilationService.getAldorCompilationService(getProject());

        String makefileText = annotationTestFixture.createMakefile(aldorExecutableRule.executable().getAbsolutePath(),
                Collections.singletonList("foo.as"),
                Collections.emptyMap());
        VirtualFile makefileFile = annotationTestFixture.createFile(getProject(), "Makefile", makefileText);
        String program = "#include \"aldor\"\n#pile\n" +
                "Foo(X: with): Category == with\n  foo: % -> %\n  bar: String -> %\n";
        VirtualFile testFile = annotationTestFixture.createFile(codeTestFixture.getProject(), "foo.as", program);

        annotationTestFixture.compileFile(testFile, getProject());

        SpadLibrary mgr = SpadLibraryManager.getInstance(getProject()).forModule(codeTestFixture.getModule(), AldorFileType.INSTANCE);
        assertNotNull(mgr);
        List<Syntax> tt = mgr.allTypes();
        System.out.println("All types: "+ tt);

        assertEquals(1, tt.size());
        assertTrue(tt.stream().map(Object::toString).collect(Collectors.toList()).contains("Foo"));

        annotationTestFixture.writeFile(getProject(), "foo.as", program + "Bar: with == add\n");
        annotationTestFixture.compileFile(testFile, getProject());

        mgr = SpadLibraryManager.getInstance(getProject()).forModule(codeTestFixture.getModule(), AldorFileType.INSTANCE);
        assertNotNull(mgr);
        List<Syntax> tt2 = mgr.allTypes();
        System.out.println("All types: "+ tt2);
        assertTrue(tt2.stream().map(Object::toString).collect(Collectors.toList()).contains("Foo"));
        assertTrue(tt2.stream().map(Object::toString).collect(Collectors.toList()).contains("Bar"));
    }

    private Project getProject() {
        return codeTestFixture.getProject();
    }


}
