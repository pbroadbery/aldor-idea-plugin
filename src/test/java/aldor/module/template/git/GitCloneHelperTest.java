package aldor.module.template.git;

import aldor.test_util.JUnits;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class GitCloneHelperTest {

    private final IdeaProjectTestFixture fixture = IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder("simple-project").getFixture();
  @Rule
    public TestRule testRule = RuleChain.emptyRuleChain()
            .around(JUnits.swingThreadTestRule())
            .around(JUnits.fixtureRule(fixture))
            .around(JUnits.setLogToDebugTestRule);

    @Test
    public void testOne() {
        VirtualFile dir = ModuleRootManager.getInstance(fixture.getModule()).getContentRoots()[0];
        Task.Backgroundable task = new Task.Backgroundable(fixture.getProject(), "clone") {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                boolean success = GitCloneHelper.clone(fixture.getProject(), "/home/pab/Work/aldorgit/aldor/aldor", dir.getPath(), "aldor");
                System.out.println("Success: "+  success);
            }

            @Override
            public void onSuccess() {
                System.out.println("Success!");
                super.onSuccess();
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                System.out.println("Error:");
                error.printStackTrace();
                super.onThrowable(error);

            }
        };
        task.queue();

    }

}