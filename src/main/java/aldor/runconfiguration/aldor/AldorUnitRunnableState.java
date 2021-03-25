package aldor.runconfiguration.aldor;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.aldor.AldorFacetConfiguration;
import aldor.build.facet.aldor.AldorFacetType;
import aldor.builder.jps.AldorModuleExtensionProperties;
import aldor.util.Mavens;
import aldor.util.StringUtilsAldorRt;
import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.target.TargetEnvironment;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ex.JavaSdkUtil;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.ui.OrderRoot;
import com.intellij.openapi.util.io.FileFilters;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.PathUtil;
import com.intellij.util.PathsList;
import com.intellij.util.io.BaseOutputReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryProperties;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class AldorUnitRunnableState extends AbstractAldorUnitRunnableState<AldorUnitConfiguration> {
    private static final Logger LOG = Logger.getInstance(AldorUnitRunnableState.class);
    public static final String FRAMEWORK_NAME = "AldorUnit";
    private final AldorUnitConfiguration configuration;

    protected AldorUnitRunnableState(AldorUnitConfiguration configuration, @NotNull ExecutionEnvironment environment) {
        super(environment);
        this.configuration = configuration;
    }

    @Override
    protected void configureVMParameters(JavaParameters javaParameters) {
        ParametersList vmParams = javaParameters.getVMParametersList();
        vmParams.defineProperty("aldor.aldorunit.implementation", StringUtilsAldorRt.trimExtension(configuration.myInputFile.getPsiElement().getName()));
        vmParams.defineProperty("aldor.aldorunit.testClass", configuration.javaClass());
        vmParams.defineProperty("aldor.aldorunit.source", configuration.myInputFile.getPsiElement().getName());
    }

    @Override
    protected void configureParameters(JavaParameters javaParameters) {
        //javaParameters.setMainClass("com.intellij.rt.execution.junit.JUnitStarter");
        javaParameters.setMainClass("com.intellij.rt.junit.JUnitStarter");

        javaParameters.getProgramParametersList().add("-ideVersion5");
        javaParameters.getProgramParametersList().add("-junit4");
        javaParameters.getProgramParametersList().add("aldor.aldorunit.AldorUnitTestWrapper");
    }

    @Override
    protected void configureRTClasspath(JavaParameters javaParameters) throws CantRunException {
        PathsList classPath = javaParameters.getClassPath();
        configureSdkClassPath(classPath);
        configureJUnitClassPath(classPath);
        configureAldorUnitClassPath(classPath);
        configureAldorClassPath(classPath);
        configureLocalClassPath(classPath);
    }

    // TODO: Throw CantRunException if needed
    @SuppressWarnings("RedundantThrows")
    private void configureAldorUnitClassPath(PathsList classPath) throws CantRunException {
        // Hardcode.
        // TODO: Use library - or builtin
        File aldorUnitJar = new File(PathManager.getPluginsPath(), "aldor-idea/aldorunit/aldorunit.jar");
        if (!aldorUnitJar.exists()) {
            LOG.error("Missing aldor unit jar - " + aldorUnitJar.getAbsolutePath());
        }
        classPath.add(aldorUnitJar);
    }

    private void configureLocalClassPath(PathsList classPath) {
        PsiFile elt = this.configuration.myInputFile.getPsiElement();
        if (elt == null) {
            LOG.error("No element to run from " + configuration.getName());
            return;
        }
        VirtualFile file = elt.getVirtualFile();
        VirtualFile sourceRoot = ProjectRootManager.getInstance(elt.getProject()).getFileIndex().getSourceRootForFile(file);
        if (sourceRoot == null) {
            LOG.error("Missing source root for file " + file);
            return;
        }
        classPath.addFirst(file.getParent().getPath() + "/out/jar/" + sourceRoot.getName() + ".jar");
    }

    private void configureAldorClassPath(PathsList classPath) {
        Module module = configuration.getConfigurationModule().getModule();
        if (module == null) {
            LOG.error("Missing module for " + this.configuration.getName());
            return;
        }
        AldorFacet facet = FacetManager.getInstance(module).getFacetByType(AldorFacetType.TYPE_ID);
        if (facet == null) {
            LOG.warn("Missing facet information");
        }
        Optional<String> sdkName = Optional.ofNullable(facet)
                .map(Facet::getConfiguration)
                .map(AldorFacetConfiguration::getState)
                .map(AldorModuleExtensionProperties::sdkName);
        Optional<Sdk> sdk = sdkName.map(name -> ProjectJdkTable.getInstance().findJdk(name));
        if (!sdk.isPresent()) {
            LOG.error("Missing aldor implementation for " + configuration.getName() + " implementation: " + sdkName.orElse("<missing>")
                    + ". Available SDKS " + Arrays.stream(ProjectJdkTable.getInstance().getAllJdks()).map(Sdk::getName).collect(Collectors.toList()));
        }
        else {
            File library = new File(sdk.get().getHomePath(), "share/lib");
            File[] jarPaths = library.listFiles(FileFilters.withExtension("jar"));
            for (File jarPath : jarPaths) {
                classPath.addFirst(jarPath.getAbsolutePath());
            }
        }
    }

    private void configureJUnitClassPath(PathsList classPath) throws CantRunException {
        Project project = this.configuration.getProject();
        Collection<OrderRoot> roots;
        try {
            roots = Mavens.downloadDependenciesWhenRequired(project,
                    new RepositoryLibraryProperties("junit", "junit", Mavens.JUNIT_VERSION));
        } catch (Mavens.MavenDownloadException e) {
            throw new CantRunException(e.getMessage() + " when running " + this.configuration.getName(), e);
        }

        for (OrderRoot root : roots) {
            //noinspection ObjectEquality
            if (root.getType() == OrderRootType.CLASSES) {
                classPath.add(root.getFile());
            }
        }}

    private void configureSdkClassPath(PathsList classPath) {
        classPath.addFirst(JavaSdkUtil.getIdeaRtJarPath());

        File platformJar = new File(PathUtil.getJarPathForClass(Project.class));
        File parentFile = platformJar.getParentFile().getAbsoluteFile();
        classPath.addFirst(new File(parentFile, "../plugins/junit/lib/junit-rt.jar").getAbsolutePath());
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
        return false; // true for junit 5
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
