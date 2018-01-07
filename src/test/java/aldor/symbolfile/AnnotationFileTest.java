package aldor.symbolfile;

import aldor.build.module.AnnotationFileManager;
import aldor.psi.AldorIdentifier;
import aldor.syntax.SyntaxPrinter;
import aldor.test_util.AldorRoundTripProjectDescriptor;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.util.AnnotatedOptional;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import junit.framework.AssertionFailedError;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AnnotationFileTest {

    private final AnnotationFileTestFixture annotationTestFixture = new AnnotationFileTestFixture();
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();
    private final CodeInsightTestFixture insightTestFixture = LightPlatformJUnit4TestRule.createFixture(getProjectDescriptor());

    @Before
    public void setUp() {
        JUnits.setLogToDebug();
    }

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(insightTestFixture, ""));
    @Test
    public void testLocalReferences() throws Exception {
        String program = "#include \"aldor\"\n"
                + "#include \"aldor\"\n" +
                "Dom: with { foo: () -> % } == add { foo(): % == never }\n" +
                "f(): Dom == foo();\n";
        VirtualFile sourceFile = annotationTestFixture.createFile(insightTestFixture.getProject(), "foo.as", program);
        annotationTestFixture.createFile(insightTestFixture.getProject(), "Makefile", "foo.abn: foo.as\n\t" + aldorExecutableRule.executable() + " -Fabn=foo.abn foo.as");

        annotationTestFixture.compileFile(sourceFile, insightTestFixture.getProject());

        annotationTestFixture.runInEdtAndWait(() -> {
            AnnotationFileManager fileManager = AnnotationFileManager.getAnnotationFileManager(insightTestFixture.getProject());

            PsiFile file = insightTestFixture.getPsiManager().findFile(sourceFile);
            assertNotNull(file);
            AldorIdentifier fooReference = PsiTreeUtil.findElementOfClassAtOffset(file, program.indexOf("foo();"), AldorIdentifier.class, true);
            AnnotatedOptional<Syme, String> fooRef = fileManager.symeForElement(fooReference);
            Syme fooSyme = fooRef.orElseThrowError(msg -> new AssertionFailedError("Missing ref for foo: " + msg));
            assertEquals("Dom", fooSyme.exporter().toString());
            assertEquals("() -> Dom", SyntaxPrinter.instance().toString(fooSyme.type()));
        });

    }

    private LightProjectDescriptor getProjectDescriptor() {
        return new AldorRoundTripProjectDescriptor();
    }


}
