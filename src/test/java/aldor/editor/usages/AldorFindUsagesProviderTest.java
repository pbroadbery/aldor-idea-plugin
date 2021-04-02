package aldor.editor.usages;

import aldor.psi.AldorDefine;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.AldorRoundTripProjectDescriptor;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.find.findUsages.DefaultFindUsagesHandlerFactory;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.usageView.UsageInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AldorFindUsagesProviderTest {
    private final ExecutablePresentRule aldorExecutableRule = new ExecutablePresentRule.Aldor();
    private final CodeInsightTestFixture codeTestFixture = LightPlatformJUnit4TestRule.createFixture(new AldorRoundTripProjectDescriptor());
    private final AnnotationFileTestFixture annotationTestFixture= new AnnotationFileTestFixture();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(aldorExecutableRule)
                    .around(new LightPlatformJUnit4TestRule(codeTestFixture, ""))
                    .around(annotationTestFixture.rule(codeTestFixture::getProject));

    @After
    public void doAfter() {
        EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
    }

    @Test
    @Ignore("Round trip descriptor is broken")
    public void testUsage() throws ExecutionException, InterruptedException {
        String makefileText = annotationTestFixture.createMakefile(aldorExecutableRule.executable().getAbsolutePath(), Collections.singleton("foo.as"));
        VirtualFile makefileFile = annotationTestFixture.createFile(codeTestFixture.getProject(), "Makefile", makefileText);
        String program = "#include \"aldor\"\n" +
                "Foo(X: with): with { id: % -> %} == add { id(x: %): % == x }\n" +
                "fn(x: Foo String): Foo String == id x\n";
        VirtualFile testFile = annotationTestFixture.createFile(codeTestFixture.getProject(), "foo.as", program);

        annotationTestFixture.compileFile(testFile, codeTestFixture.getProject());

        EdtTestUtil.runInEdtAndWait(() -> {
            PsiFile psiFile = PsiManager.getInstance(codeTestFixture.getProject()).findFile(testFile);
            assertNotNull(psiFile);
            PsiElement elt = PsiTreeUtil.findElementOfClassAtOffset(psiFile, program.indexOf("Foo("), AldorDefine.class, true);
            assertNotNull(elt);
            Collection<UsageInfo> usages = codeTestFixture.findUsages(elt);
            System.out.println("Usages of Foo: " + usages);

            PsiElement2UsageTargetAdapter adapter = usageTargetForElement(elt);
            System.out.println("Location: " + adapter.getLocationString()
             + "\nDescription: " + adapter.getLongDescriptiveName()
             + "\nPresentableText: " + adapter.getPresentableText());

            assertEquals("Constant 'Foo X' in Project Files", adapter.getLongDescriptiveName());
        });

    }

    @NotNull
    private PsiElement2UsageTargetAdapter usageTargetForElement(PsiElement elt) {
        FindUsagesHandler handler = new DefaultFindUsagesHandlerFactory().createFindUsagesHandler(elt, false);
        assertNotNull(handler);
        FindUsagesOptions options = handler.getFindUsagesOptions();

        return new PsiElement2UsageTargetAdapter(elt, options);
    }

}
