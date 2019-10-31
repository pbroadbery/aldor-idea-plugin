package aldor.references;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AldorRoundTripReferenceTest {

    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(SdkProjectDescriptors.aldorSdkProjectDescriptor(aldorExecutableRule.prefix()));
    private final AnnotationFileTestFixture annotationTestFixture= new AnnotationFileTestFixture();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(annotationTestFixture.rule(codeTestFixture::getProject));

    @Before
    public void doBefore() {
        JUnits.setLogToDebug();
    }

    @After
    public void doAfter() {
        EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testResolution() throws ExecutionException, InterruptedException {
        String makefileText = annotationTestFixture.createMakefile(aldorExecutableRule.executable().getAbsolutePath(),
                                                                    Arrays.asList("define.as", "use.as"),
                ImmutableMap.<String, List<String>>builder().put("use", Collections.singletonList("define")).build());
        VirtualFile makefileFile = annotationTestFixture.createFile(codeTestFixture.getProject(), "Makefile", makefileText);
        String definitions = "#include \"aldor\"\n" +
                "+++ This is a domain\n" +
                "define Dom(R: PrimitiveType): with { f: R -> Boolean;\n" +
                " ++ This is a function\n" +
                " } == add { f(x: R): Boolean == x=x }\n";

        String uses = "#include \"aldor\"\n" +
                "#library DEF \"define.ao\" \n" +
                "import from DEF;\n" +
                "import from Dom Integer, Integer;\n" +
                "f(12)\n";
        VirtualFile defFile = annotationTestFixture.createFile(codeTestFixture.getProject(), "define.as", definitions);
        VirtualFile useFile = annotationTestFixture.createFile(codeTestFixture.getProject(), "use.as", uses);

        annotationTestFixture.compileFile(defFile, codeTestFixture.getProject());
        annotationTestFixture.compileFile(useFile, codeTestFixture.getProject());

        EdtTestUtil.runInEdtAndWait(() -> {
            PsiFile usePsiFile = PsiManager.getInstance(codeTestFixture.getProject()).findFile(useFile);
            assertNotNull(usePsiFile);
            AldorIdentifier idCall = PsiTreeUtil.findElementOfClassAtOffset(usePsiFile, uses.indexOf("f(12)"), AldorIdentifier.class, true);

            PsiElement fnCallResolved = idCall.getReference().resolve();
            assertTrue(fnCallResolved instanceof AldorDeclare);
            assertEquals(definitions.indexOf("f: R"), fnCallResolved.getTextOffset());

            AldorIdentifier pkgRef = PsiTreeUtil.findElementOfClassAtOffset(usePsiFile, uses.indexOf("Dom Integer"), AldorIdentifier.class, true);
            PsiElement packageResolved = pkgRef.getReference().resolve();
            assertTrue(packageResolved instanceof AldorDefine);
            assertEquals(definitions.indexOf("Dom(R:"), packageResolved.getTextOffset());

        });
    }


}
