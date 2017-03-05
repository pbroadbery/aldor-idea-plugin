package aldor.symbolfile;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AnnotationFileManager;
import aldor.util.VirtualFileTests;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.EdtTestUtilKt;
import one.util.streamex.Joining;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class AnnotationFileTestFixture {
    private final Map<String, VirtualFile> fileForName = Maps.newHashMap();
    @Nullable
    private Project project;

    AnnotationFileTestFixture(@Nullable Project project) {
        this.project = project;
    }

    public AnnotationFileTestFixture() {
        project = null;
    }

    public TestRule rule(Supplier<Project> projectSupplier) {
        return new Rule(projectSupplier);
    }

    public VirtualFile createFile(String name, String text) {
        Assert.assertNotNull(project);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile file = VirtualFileTests.createFile(project.getBaseDir(), name, text);
            fileForName.put(name, file);
        });
        return fileForName.get(name);
    }

    public void project(Project project) {
        this.project = project;
    }

    public VirtualFile fileForName(String name) {
        return fileForName.get(name);
    }

    public void compileFile(VirtualFile file) throws ExecutionException, InterruptedException {
        Assert.assertNotNull(project);
        List<Future<Void>> result = Lists.newArrayList();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            Module module = AldorModuleManager.getInstance(project).aldorModules().stream().findFirst().orElseThrow(()->new RuntimeException("no module for " + file));
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            Optional<AnnotationFileManager> manager = AnnotationFileManager.getAnnotationFileManager(module);
            assert manager.isPresent();

            Future<Void> fut = manager.get().requestRebuild(psiFile);
            result.add(fut);
        });

        result.get(0).get();
    }

    public String createMakefile(String aldorLocation, Collection<String> files) {
        String abnRule =
                "$(addsuffix .abn, $(ALDOR_FILES)): %.abn: %.as\n" +
                "\techo Making $@\n"+
                "\t$(ALDOR) -Fabn=$@ $<\n";
        String text = "ALDOR = %s\nALDOR_FILES=%s\n%s\n";
        return String.format(text, aldorLocation, files.stream().map(StringUtil::trimExtension).collect(Joining.with(" ")), abnRule);
    }

    public void runInEdtAndWait(@NotNull Runnable runnable) throws Exception {
        EdtTestUtilKt.runInEdtAndWait(() -> {
            runnable.run();
            //noinspection ReturnOfNull
            return null;
        });
    }

    private class Rule implements TestRule {
        private final Supplier<Project> projectSupplier;

        public Rule(Supplier<Project> projectSupplier) {
            this.projectSupplier = projectSupplier;
        }

        @Override
        public Statement apply(Statement statement, Description description) {
            //noinspection InnerClassTooDeeplyNested
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    AnnotationFileTestFixture.this.project(projectSupplier.get());
                    statement.evaluate();
                }
            };
        }
    }

}
