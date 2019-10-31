package aldor.runconfiguration.aldor;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.jarRepository.JarRepositoryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ex.JavaSdkUtil;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.OrderRoot;
import com.intellij.util.PathUtil;
import com.intellij.util.PathsList;
import com.intellij.util.io.BaseOutputReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryProperties;

import java.io.File;
import java.util.Collection;

// NB: Use a concrete 'T' when one shows up...
public class AldorUnitRunnableState extends AbstractAldorUnitRunnableState<AldorUnitConfiguration> {
    public static final String FRAMEWORK_NAME = "AldorUnit";
    private static final String JUNIT_VERSION = "4.12";
    private final AldorUnitConfiguration configuration;

    protected AldorUnitRunnableState(AldorUnitConfiguration configuration, @NotNull ExecutionEnvironment environment) {
        super(environment);
        this.configuration = configuration;
    }

    @Override
    protected void configureParameters(JavaParameters javaParameters) {
        javaParameters.setMainClass("com.intellij.rt.execution.junit.JUnitStarter");
        javaParameters.getProgramParametersList().add("-ideVersion5");
        javaParameters.getProgramParametersList().add("-junit4");
        javaParameters.getProgramParametersList().add(configuration.javaClass());
    }

    @Override
    protected void configureRTClasspath(JavaParameters javaParameters) throws CantRunException {
        configureSdkClassPath(javaParameters.getClassPath());
        PathsList classPath = javaParameters.getClassPath();
        configureJUnitClassPath(classPath);
    }

    private void configureJUnitClassPath(PathsList classPath) throws CantRunException {
        Project project = this.configuration.getProject();
        downloadDependenciesWhenRequired(project, classPath,
                new RepositoryLibraryProperties("junit", "junit", JUNIT_VERSION));
    }

    private void configureSdkClassPath(PathsList classPath) {
        // /usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/java -ea -Didea.load.plugins.id=pab.aldor
// -Didea.home.path=/home/pab/.IdeaIC2018.3/system/plugins-sandbox/test
// -Didea.plugins.path=/home/pab/.IdeaIC2018.3/system/plugins-sandbox/plugins
// -Didea.test.cyclic.buffer.size=1048576
// -javaagent:/home/pab/Work/intellij/idea-IC-183.5912.21/lib/idea_rt.jar=44791:/home/pab/Work/intellij/idea-IC-183.5912.21/bin
// -Dfile.encoding=UTF-8
// -classpath /usr/lib/jvm/java-1.8.0-openjdk-amd64/lib/tools.jar
//     :/home/pab/Work/intellij/idea-IC-183.5912.21/lib/idea_rt.jar
//     :/home/pab/Work/intellij/idea-IC-183.5912.21/plugins/junit/lib/junit-rt.jar
//     :/home/pab/Work/intellij/idea-IC-183.5912.21/plugins/junit/lib/junit5-rt.jar
//     :/home/pab/Work/intellij/idea-IC-183.5912.21/lib/commons-codec-1.10.jar
//     :/home/pab/Work/intellij/idea-IC-183.5912.21/lib/external-system-impl.jar
//     :/home/pab/Work/intellij/idea-IC-183.5912.21/lib/junit-4.12.jar
//     com.intellij.rt.execution.junit.JUnitStarter -ideVersion5
//             -junit4 aldor.runconfiguration.aldor.AldorUnitConfigurationProducerTest
        File platformJar = new File(PathUtil.getJarPathForClass(Project.class));
        File parentFile = platformJar.getParentFile().getAbsoluteFile();
        classPath.addFirst(JavaSdkUtil.getIdeaRtJarPath());
        classPath.addFirst(new File(parentFile, "../plugins/junit/lib/junit-rt.jar").getAbsolutePath());
    }

    private static void downloadDependenciesWhenRequired(Project project,
                                                         PathsList classPath,
                                                         RepositoryLibraryProperties properties) throws CantRunException {
        Collection<OrderRoot> roots =
                JarRepositoryManager.loadDependenciesModal(project, properties, false, false, null, null);
        if (roots.isEmpty()) {
            throw new CantRunException("Failed to resolve " + properties.getMavenId());
        }
        for (OrderRoot root : roots) {
            //noinspection ObjectEquality
            if (root.getType() == OrderRootType.CLASSES) {
                classPath.add(root.getFile());
            }
        }
    }

    @Override
    protected String getFrameworkName() {
        return FRAMEWORK_NAME;
    }

    @NotNull
    @Override
    protected AldorUnitConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    protected boolean isIdBasedTestTree() {
        return true;
    }

    @Override
    @NotNull
    protected OSProcessHandler createHandler(Executor executor) throws ExecutionException {
        OSProcessHandler processHandler = new KillableColoredProcessHandler(createCommandLine()) {
            @NotNull
            @Override
            protected BaseOutputReader.Options readerOptions() {
                return BaseOutputReader.Options.forMostlySilentProcess();
            }
        };
        ProcessTerminatedListener.attach(processHandler);
        /*
        final SearchForTestsTask searchForTestsTask = createSearchingForTestsTask();
        if (searchForTestsTask != null) {
            searchForTestsTask.attachTaskToProcess(processHandler);
        }
        */
        return processHandler;
    }
}
