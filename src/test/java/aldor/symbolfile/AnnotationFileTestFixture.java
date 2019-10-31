package aldor.symbolfile;

import aldor.build.module.AnnotationFileManager;
import aldor.util.VirtualFileTests;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.EdtTestUtilKt;
import com.intellij.testFramework.fixtures.impl.BaseFixture;
import one.util.streamex.Joining;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Track annotation files.
 *
 * Note that for JUnit3 you will need to ensure the .iws and .iml files are kept when UsefulTestCase
 * comes to delete things
 */
public class AnnotationFileTestFixture extends BaseFixture {
    private static final Logger LOG = Logger.getInstance(AnnotationFileTestFixture.class);

    private final Map<String, VirtualFile> fileForName = Maps.newHashMap();

    AnnotationFileTestFixture(@Nullable Project project) {
    }

    public AnnotationFileTestFixture() {
    }

    public TestRule rule(Supplier<Project> projectSupplier) {
        //noinspection ReturnOfInnerClass
        return new Rule(projectSupplier);
    }

    public VirtualFile createFile(Project project, String name, String text) {
        Assert.assertNotNull(project);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile file = VirtualFileTests.createFile(sourceDirectory(project), name, text);
            fileForName.put(name, file);
        });
        return fileForName.get(name);
    }

    public void writeFile(Project project, String name, String text) {
        Assert.assertNotNull(project);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile file = fileForName.get(name);
            VirtualFileTests.writeFile(file, text.getBytes(StandardCharsets.UTF_8));
        });
    }


    public VirtualFile sourceDirectory(Project project) {
        return ProjectRootManager.getInstance(project).getContentSourceRoots()[0];
    }

    public VirtualFile fileForName(String name) {
        return fileForName.get(name);
    }

    public void compileFile(VirtualFile file, Project project) throws ExecutionException, InterruptedException {
        file.getFileSystem().refresh(false);

        Assert.assertNotNull(project);
        List<Future<Void>> result = Lists.newArrayList();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            AnnotationFileManager manager = AnnotationFileManager.getAnnotationFileManager(project);

            Future<Void> fut = manager.requestRebuild(psiFile);
            result.add(fut);
        });

       // file.getFileSystem().refresh(false);
        result.get(0).get();
        LOG.info("START REFRESH");
        file.getFileSystem().refresh(false);
        LOG.info("END REFRESH");
    }

    public String createMakefile(String aldorLocation, Collection<String> files) {
        return createMakefile(aldorLocation, files, Collections.emptyMap());
    }

    public String createMakefile(String aldorLocation, Collection<String> files, Map<String, List<String>> dependencies) {
        String aoRule =
                "$(patsubst %,out/ao/%.ao, $(ALDOR_FILES)): out/ao/%.ao: %.as\n" +
                "\techo Making $@ - $^\n"+
                "\tmkdir -p out/ao\n" +
                "\t$(ALDOR) -Y out/ao -Fasy -Fao=out/ao/$*.ao -Fabn=out/ao/$*.abn $*.as\n" +
                "\n";
        String text = "ALDOR = %s\nALDOR_FILES=%s\n%s\n%s\n";
        String dependencyRules = dependencies.entrySet().stream()
                                                        .map(e -> "out/ao/" + StringUtil.trimExtensions(e.getKey()) + ".ao: "
                                                                + e.getValue().stream()
                                                                        .map(StringUtil::trimExtensions)
                                                                        .map(x -> "out/ao/" + x + ".ao").collect(Joining.with(" ")))
                                                                        .collect(Joining.with("\n"));
        String makefile =  String.format(text, aldorLocation,
                                         files.stream().map(StringUtil::trimExtensions).collect(Joining.with(" ")),
                                         dependencyRules,
                                         aoRule);

        LOG.info("Makefile: " + makefile);
        return makefile;
    }

    public void runInEdtAndWait(@NotNull Runnable runnable) {
        EdtTestUtilKt.runInEdtAndWait(() -> {
            runnable.run();
            //noinspection ReturnOfNull
            return null;
        });
    }

    private class Rule implements TestRule {
        private final Supplier<Project> projectSupplier;

        Rule(Supplier<Project> projectSupplier) {
            this.projectSupplier = projectSupplier;
        }

        @Override
        public Statement apply(Statement statement, Description description) {
            //noinspection InnerClassTooDeeplyNested
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    try {
                        statement.evaluate();
                    } finally {
                        System.out.println("Done...");
                    }
                }
            };
        }

    }
}
