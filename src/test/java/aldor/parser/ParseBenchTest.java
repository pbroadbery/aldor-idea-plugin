package aldor.parser;

import aldor.psi.elements.AldorTypes;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SkipOnCIBuildRule;
import aldor.test_util.Timer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.File;
import java.util.List;

import static aldor.test_util.TestFiles.existingFile;

public class ParseBenchTest {
    public static final int ITERATIONS = 500;
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);
    @Rule
    public final TestRule rule = new SkipOnCIBuildRule();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testParseFF2GE() throws Exception {
        Assert.assertNotNull(testFixture.getProject());

        Project project = testFixture.getProject();
        Timer timer = new Timer("Parser");
        try (Timer.TimerRun run = timer.run()) {
            for (int i = 0; i< ITERATIONS; i++) {
                File file = existingFile("/home/pab/Work/aldorgit/aldor/aldor/lib/algebra/src/mat/gauss/sit_ff2ge.as");
                final List<PsiErrorElement> errors = parseFile(project, file);
                Assert.assertEquals(0, errors.size());
            }
        }
        System.out.println(String.format("Number of iterations: %s - Total time: %s - Per Iteration: %s", ITERATIONS, timer.duration(), ((double) timer.duration())/ITERATIONS));

    }

    @NotNull
    private List<PsiErrorElement> parseFile(Project project, File file) {
        Assert.assertTrue(file.exists());
        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
        Assert.assertNotNull(vf);
        PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
        //noinspection ConstantConditions
        String text = psiFile.getText();

        PsiElement psi = parseText(text);
        return ParserFunctions.getPsiErrorElements(psi);
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(testFixture.getProject(), text, AldorTypes.TOP_LEVEL);
    }

}
