package aldor.references;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.symbolfile.AldorRoundTripProjectDescriptor;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
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

    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(new AldorRoundTripProjectDescriptor());
    private final AnnotationFileTestFixture annotationTestFixture= new AnnotationFileTestFixture();
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(annotationTestFixture.rule(codeTestFixture::getProject));
    @Test
    public void testResolution() throws ExecutionException, InterruptedException {
        String makefileText = annotationTestFixture.createMakefile(aldorExecutableRule.executable().getAbsolutePath(),
                                                                    Arrays.asList("define.as", "use.as"),
                ImmutableMap.<String, List<String>>builder().put("use", Collections.singletonList("define")).build());
        VirtualFile makefileFile = annotationTestFixture.createFile("Makefile", makefileText);
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
        VirtualFile defFile = annotationTestFixture.createFile("define.as", definitions);
        VirtualFile useFile = annotationTestFixture.createFile("use.as", uses);

        annotationTestFixture.compileFile(defFile);
        annotationTestFixture.compileFile(useFile);

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
