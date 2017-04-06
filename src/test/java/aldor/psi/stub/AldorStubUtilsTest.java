package aldor.psi.stub;

import aldor.parser.SwingThreadTestRule;
import aldor.psi.AldorDeclare;
import aldor.psi.elements.AldorElementTypeFactory;
import aldor.test_util.LightPlatformJUnit4TestRule;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static aldor.util.VirtualFileTests.createFile;
import static com.intellij.testFramework.LightPlatformTestCase.getSourceRoot;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class AldorStubUtilsTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new SwingThreadTestRule());

    @Test
    public void definingForm() throws Exception {
        Project project = testFixture.getProject();
        String text = "Foo: with { x: String } == add";
        VirtualFile file = createFile(getSourceRoot(), "foo.as", text);
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        assertNotNull(psiFile);

        StubElement<?> stubTree = AldorElementTypeFactory.ALDOR_FILE_ELEMENT_TYPE.getBuilder().buildStubTree(psiFile);

        AldorDeclare decl = PsiTreeUtil.findElementOfClassAtOffset(psiFile, text.indexOf("x:"), AldorDeclare.class, true);
        assertNotNull(decl);

        // definingForm not implemented.. so don't expect anything.
        assertFalse(AldorStubUtils.definingForm(decl.getStub()).isPresent());
    }

}
