package aldor.symbolfile;

import aldor.annotations.AnnotationFileManager;
import aldor.util.Mavens;
import aldor.util.VirtualFileTests;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.ui.OrderRoot;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.EdtTestUtilKt;
import com.intellij.testFramework.fixtures.impl.BaseFixture;
import com.intellij.util.PathUtil;
import one.util.streamex.Joining;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryProperties;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Track annotation files.
 *
 * Note that for JUnit3 you will need to ensure the .iws and .iml files are kept when UsefulTestCase
 * comes to delete things
 */
public class AnnotationFileTestFixture extends BaseFixture {
    private static final Logger LOG = Logger.getInstance(AnnotationFileTestFixture.class);

    private final Map<String, VirtualFile> fileForName = Maps.newHashMap();

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
        LOG.info("START PRE REFRESH");
        file.getFileSystem().refresh(false);
        LOG.info("END PRE REFRESH");

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
        LOG.info("START POST REFRESH");
        file.getFileSystem().refresh(false);
        LOG.info("END POST REFRESH");
    }

    public String createMakefile(String aldorLocation, Collection<String> files) {
        return createMakefile(aldorLocation, files, Collections.emptyMap());
    }

    public String createMakefile(String aldorLocation, Collection<String> files, Map<String, List<String>> dependencies) {
        return new MakefileBuilder(aldorLocation, files).withDependencies(dependencies).build();
    }

    public void runInEdtAndWait(@NotNull Runnable runnable) {
        EdtTestUtilKt.runInEdtAndWait(() -> {
            runnable.run();
            //noinspection ReturnOfNull
            return null;
        });
    }

    public MakefileBuilder makefileBuilder(File aldorRoot, Collection<String> files) {
        return new MakefileBuilder(aldorRoot.getAbsolutePath(), files);
    }

    private class Rule implements TestRule {
        private final Supplier<Project> projectSupplier;

        Rule(Supplier<Project> projectSupplier) {
            this.projectSupplier = projectSupplier;
        }

        @Override
        public Statement apply(Statement statement, Description description) {
            //noinspection InnerClassTooDeeplyNested,ReturnOfInnerClass
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    boolean save = ApplicationManagerEx.getApplicationEx().isSaveAllowed();
                    try {
                        ApplicationManagerEx.getApplicationEx().setSaveAllowed(true);
                        LOG.info("SAVE PROJECT " + save);
                        projectSupplier.get().save();
                        statement.evaluate();
                    } finally {
                        ApplicationManagerEx.getApplicationEx().setSaveAllowed(save);
                        LOG.info("Done...");
                    }
                }
            };
        }
    }

    public static class MakefileBuilder {
        private final Collection<String> definitions = new ArrayList<>();
        private final Collection<String> rules = new ArrayList<>();
        @SuppressWarnings("FieldCanBeLocal")
        private final List<String> names;
        private Project project = null;
        private VirtualFile sourceDirectory = null;
        private boolean javaRules = false;

        public MakefileBuilder(String aldorLocation, Collection<String> files) {
            names = files.stream().map(StringUtil::trimExtensions).collect(Collectors.toList());

            definitions.add("ALDOR = " + aldorLocation);
            definitions.add("ALDOR_FILES=" + Joiner.on(" ").join(names));
            definitions.add("CLASSPATH=");
            String aoRule =
                    "$(patsubst %,out/ao/%.ao, $(ALDOR_FILES)): out/ao/%.ao: %.as\n" +
                            "\techo Making $@ - $^\n"+
                            "\tmkdir -p out/ao\n" +
                            "\t$(ALDOR) -Y out/ao -Fasy -Fao=out/ao/$*.ao -Fabn=out/ao/$*.abn $*.as\n" +
                            "\n";
            rules.add(aoRule);
        }

        public MakefileBuilder withProject(Project project) {
            this.project = project;
            return this;
        }

        public MakefileBuilder withDependencies(Map<String, List<String>> dependencies) {
            List<String> depRules = dependencies.entrySet().stream()
                    .map(e -> "out/ao/" + StringUtil.trimExtensions(e.getKey()) + ".ao: "
                            + e.getValue().stream()
                            .map(StringUtil::trimExtensions)
                            .map(x -> "out/ao/" + x + ".ao").collect(Joining.with(" ")))
                    .collect(Collectors.toList());
            rules.addAll(depRules);
            return this;
        }

        public MakefileBuilder withSourceDirectory(VirtualFile sourceDirectory) {
            this.sourceDirectory = sourceDirectory;
            return this;
        }

        public MakefileBuilder withJavaRules() {
            if (!javaRules) {
                javaRules = true;

                String javaRule =
                                "$(patsubst %, out/java/aldorcode/%.java, $(ALDOR_FILES)): out/java/aldorcode/%.java: out/ao/%.ao\n" +
                                "\tmkdir -p out/java\n" +
                                "\t$(ALDOR) -Fjava=out/java/$*.java out/ao/$*.ao";
                rules.add(javaRule);
            }
            return this;
        }

        public MakefileBuilder withAldorUnit() {
            /* // TODO: Use library or builtin aldorunit
            Sdk aldorUnitSdk = type.aldorUnitSdk(sdk);
            assert(aldorUnitSdk != null);
            definitions.add("CLASSPATH += " + aldorUnitSdk.getHomePath() + "/aldorunit.jar");
             */

            try {
                Collection<OrderRoot> roots = Mavens.downloadDependenciesWhenRequired(project,
                        new RepositoryLibraryProperties("junit", "junit", Mavens.JUNIT_VERSION));
                for (OrderRoot root : roots) {
                    definitions.add("CLASSPATH += " + PathUtil.getLocalPath(root.getFile()));
                }
            } catch (Mavens.MavenDownloadException e) {
                Assert.fail(e.getMessage());
            }


            return this;
        }

        public MakefileBuilder withJarRule() {
            if (sourceDirectory == null) {
                throw new RuntimeException("Missing source directory");
            }
            withJavaRules();

            String jarDef = "JAR=" + sourceDirectory.getName() + ".jar";
            definitions.add(jarDef);

            String jarRule = "out/jar/$(JAR): $(patsubst %, out/java/aldorcode/%.java, $(ALDOR_FILES))\n" +
                    "\tmkdir -p out/jar\n" +
                    "\t(cd out/java; javac -cp $$(echo $(CLASSPATH) /home/pab/Work/aldorgit/utypes/opt/share/lib/*.jar | tr ' ' ':') $$(find . -name \\*.java))\n" +
                    "\t(cd out/java; jar cf ../jar/$(JAR) .)\n";
            rules.add(jarRule);
            return this;
        }


        public String build() {
            return Joiner.on("\n").join(definitions) + "\n# Rules\n" + Joiner.on("\n\n").join(rules);
        }

    }

}
