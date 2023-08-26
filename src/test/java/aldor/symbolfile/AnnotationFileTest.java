package aldor.symbolfile;

import aldor.annotations.AnnotationFileManager;
import aldor.annotations.AnnotationFileNavigator;
import aldor.annotations.DefaultAnnotationFileNavigator;
import aldor.psi.AldorIdentifier;
import aldor.syntax.SyntaxPrinter;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import aldor.test_util.SourceFileStorageType;
import aldor.util.AnnotatedOptional;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AnnotationFileTest {

    private AnnotationFileTestFixture annotationFileTestFixture = null;
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();
    private final CodeInsightTestFixture insightTestFixture = LightPlatformJUnit4TestRule.createFixture(SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorExecutableRule, SourceFileStorageType.Real));

    @After
    public void doAfter() {
        EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
    }

    AnnotationFileTestFixture annotationFileTestFixture() {
        if (annotationFileTestFixture == null) {
            annotationFileTestFixture = new AnnotationFileTestFixture();
        }
        return annotationFileTestFixture;
    }

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(insightTestFixture, ""))
                    .around(JUnits.prePostTestRule(() -> {
                        ApplicationManagerEx.getApplicationEx().setSaveAllowed(true);
                        insightTestFixture.getProject().save();
                    }, () -> {}))
                    .around(annotationFileTestFixture().rule(insightTestFixture::getProject))
                    .around(JUnits.setLogToDebugTestRule);
    @Test
    public void testLocalReferences() throws Exception {
        String program = """
                #include "aldor"
                #include "aldor"
                Dom: with { foo: () -> % } == add { foo(): % == never }
                f(): Dom == foo();
                """;
        VirtualFile sourceFile = annotationFileTestFixture().createFile(insightTestFixture.getProject(), "foo.as", program);
        annotationFileTestFixture().createFile(insightTestFixture.getProject(), "Makefile",
                "out/ao/foo.ao: foo.as\n\tmkdir -p out/ao\n\t" + aldorExecutableRule.executable() + " -Fao=out/ao/foo.ao -Fabn=out/ao/foo.abn foo.as");

        annotationFileTestFixture().compileFile(sourceFile, insightTestFixture.getProject());

        annotationFileTestFixture().runInEdtAndWait(() -> {
            AnnotationFileManager fileManager = AnnotationFileManager.getAnnotationFileManager(insightTestFixture.getProject());
            AnnotationFileNavigator navigator = new DefaultAnnotationFileNavigator(fileManager);
            PsiFile file = insightTestFixture.getPsiManager().findFile(sourceFile);
            assertNotNull(file);
            AldorIdentifier fooReference = PsiTreeUtil.findElementOfClassAtOffset(file, program.indexOf("foo();"), AldorIdentifier.class, true);
            AnnotatedOptional<Syme, String> fooRef = navigator.symeForElement(fooReference);
            Syme fooSyme = fooRef.orElseThrowError(msg -> new AssertionFailedError("Missing ref for foo: " + msg));
            assertEquals("Dom", fooSyme.exporter().toString());
            assertEquals("() -> Dom", SyntaxPrinter.instance().toString(fooSyme.type()));
        });

    }

}
