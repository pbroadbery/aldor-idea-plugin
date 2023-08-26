package aldor.module.template.detect;

import aldor.annotations.AnnotationFileManager;
import aldor.build.builders.AldorBuildTargetScopeProvider;
import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.Id;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.util.Try;
import com.intellij.compiler.impl.OneProjectItemCompileScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestLoggerFactory;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Assume;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AldorRepoProjectDetectorBuildTest extends AssumptionAware.ImportFromSourcesTestCase {
    private static final Logger LOG = Logger.getInstance(AldorRepoProjectDetectorBuildTest.class);
    private VirtualFile vpath;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        LOG.info("Starting " + this.getClass().getCanonicalName());
        Assume.assumeTrue(ExecutablePresentRule.AldorDev.INSTANCE.shouldRunTest());
        vpath = cloneRepository(ExecutablePresentRule.AldorDev.INSTANCE.repoRoot());
    }

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }

    @Override
    public void tearDown() throws Exception {
        try {
            TestLoggerFactory.dumpLogToStdout(this.getClass().getCanonicalName());
            EdtTestUtil.runInEdtAndWait(JavaAwareProjectJdkTableImpl::removeInternalJdkInTests);
        } finally {
            super.tearDown();
        }
    }

    @Override
    protected @Nullable Sdk getTestProjectJdk() {
        //noinspection removal
        return JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();
    }

    @Override
    protected void setUpProject() {
        super.setUpProject();
    }

    public void test_Create() throws IOException {
        ApplicationManager.getApplication().invokeAndWait(() -> {
                    importFromSources(new File(vpath.getPath()));
                    Module module = ModuleUtil.findModuleForFile(vpath.findFileByRelativePath("aldor/aldor"), getProject());
                    Assert.assertNotNull(module);
                });
        Collection<Try<Void>> overall = new ArrayList<>();
        // Todo: Find a way of including lots of tests here
        ApplicationManager.getApplication().runReadAction(() -> {overall.add(Try.of(this::buildTestOneFileScope));});

        overall.add(Try.ofUnsafe( () -> buildTestViaAnnotationManager()));
        overall.add(Try.ofUnsafe( () -> buildTestLibraryLookup()));
        for (var result: overall) {
            result.orElse(e -> {e.printStackTrace(); return null;});
        }
        for (var result: overall) {
            result.orElseThrow( e -> new RuntimeException(e));
        }
    }

    private Void buildTestOneFileScope() {
        VirtualFile subPath = vpath.findFileByRelativePath("aldor/aldor/lib/aldor/src/base/sal_partial.as");
        Assert.assertTrue((subPath != null) && subPath.exists());
        CompileScope scope = new OneProjectItemCompileScope(getProject(), subPath);

        AldorBuildTargetScopeProvider provider = new AldorBuildTargetScopeProvider();
        var scopes = provider.getBuildTargetScopes(scope, getProject(), false);
        LOG.info("Scopes: " + scopes);

        return null;
    }

    public Void buildTestViaAnnotationManager() throws Exception {
        VirtualFile subPath = vpath.findFileByRelativePath("aldor/aldor/lib/aldor/src/base/sal_partial.as");
        Assert.assertTrue((subPath != null) && subPath.exists());
        var psiFile = ApplicationManager.getApplication().runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(getProject()).findFile(subPath));
        CompletableFuture<Object> ff = new CompletableFuture<>();

        ApplicationManager.getApplication().invokeLater(() -> {
            var fut = AnnotationFileManager.getAnnotationFileManager(getProject()).requestRebuild(psiFile);
            fut.whenComplete((v, e) -> ff.complete(v));
        });
        ff.get();
        return null;
    }

    public Void buildTestLibraryLookup() throws Exception {
        VirtualFile subPath = Objects.requireNonNull(vpath.findFileByRelativePath("aldor/aldor/lib/aldor/src/base/sal_partial.as"));
        var psiFile = ApplicationManager.getApplication().runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(getProject()).findFile(subPath));

        CompletableFuture<Object> ff = new CompletableFuture<>();

        ApplicationManager.getApplication().invokeLater(() -> {
            var fut = AnnotationFileManager.getAnnotationFileManager(getProject()).requestRebuild(psiFile);
            fut.whenComplete((v, e) -> ff.complete(v));
        });
        ff.get();

        ApplicationManager.getApplication().runReadAction((Computable<PsiFile>) () -> {
            PsiFile file = PsiManager.getInstance(getProject()).findFile(subPath);
            int index = file.getText().indexOf("Partial(T:Type)");
            PsiElement elt = Objects.requireNonNull(file.findElementAt(index));
            SpadLibrary library = SpadLibraryManager.getInstance(getProject()).spadLibraryForElement(elt);
            Syntax syntax = SyntaxPsiParser.parse(elt);
            Syntax id = Objects.requireNonNull(SyntaxUtils.leadingId(syntax));
            Assert.assertEquals("foo", library.definingFile(id.as(Id.class)));

            Assert.assertFalse(library.allParents(id).getFirst().isEmpty());
            return null;
        });
        return null;
    }

    private VirtualFile cloneRepository(String dirToClone) {
        LOG.info("Cloning ");
        VirtualFile vpath = getTempDir().createVirtualDir();
        Git git = Git.getInstance();
        GitLineHandler handler = new GitLineHandler(null, vpath, GitCommand.CLONE);
        handler.addLineListener( (l,type) -> LOG.info("Line: "+ type + "--> " + l));
        handler.addParameters(dirToClone);

        GitCommandResult result = git.runCommand(handler);
        LOG.info("Result " + result);
        return vpath;
    }

}