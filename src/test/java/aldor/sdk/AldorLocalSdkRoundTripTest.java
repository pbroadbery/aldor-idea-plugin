package aldor.sdk;

import aldor.module.template.GitProcess;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.symbolfile.AnnotationFileTestFixture;
import aldor.test_util.DirectoryPresentRule;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SdkProjectDescriptors;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static aldor.test_util.LightPlatformJUnit4TestRule.createFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AldorLocalSdkRoundTripTest {
    private final DirectoryPresentRule directoryPresentRule = new DirectoryPresentRule("/home/pab/tmp/plugin/test/aldor_git");
    private final CodeInsightTestFixture testFixture = createFixture(SdkProjectDescriptors.aldorLocalSdkProjectDescriptor(directoryPresentRule.path()));
    private final AnnotationFileTestFixture annotationTextFixture = new AnnotationFileTestFixture();

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(directoryPresentRule)
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new GitResetRule(directoryPresentRule.path()));

    @Before
    public void before() {
        JUnits.setLogToInfo();
    }


    @After
    public void doAfter() {
        EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
    }

    @Test
    public void testMakeSimpleChange() throws ExecutionException, InterruptedException {
        // Lets add a definition to a file..
        PsiFile[] candidate = new PsiFile[1];
        Project project = testFixture.getProject();

        EdtTestUtil.runInEdtAndWait(() -> {
                    PsiFile[] files = FilenameIndex.getFilesByName(project, "sal_lang.as", GlobalSearchScopesCore.projectProductionScope(project));
                    assertEquals(1, files.length);

                    PsiFile langAs = files[0];
                    candidate[0] = files[0];
                    String whole = langAs.getText() + "\nWibble: Type == with\n";

                    ApplicationManager.getApplication().runWriteAction(() -> {
                        try {
                            langAs.getVirtualFile().setBinaryContent(whole.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                });

        this.annotationTextFixture.compileFile(candidate[0].getVirtualFile(), project);

        EdtTestUtil.runInEdtAndWait(() -> {

            FileBasedIndex.getInstance().requestRebuild(StubUpdatingIndex.INDEX_ID);
                    FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, project, null);

                    Collection<String> ll = AldorDefineTopLevelIndex.instance.getAllKeys(project);
                    assertTrue(ll.contains("Wibble"));
        });

    }

    static class GitResetRule implements TestRule {
        private final String path;
        private final GitProcess process = new GitProcess();

        public GitResetRule(String path) {
            this.path = path;
        }

        @Override
        public Statement apply(Statement statement, Description description) {
            return JUnits.prePostStatement(this::reset, () -> {}, statement);
        }

        private void reset() throws IOException {
            process.runCommand(new File(path + "/aldor"), Lists.newArrayList("git", "reset", "--hard"));
        }
    }
}
