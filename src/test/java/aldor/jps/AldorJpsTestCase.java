/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aldor.jps;

import aldor.builder.jps.AldorModuleExtensionProperties;
import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.JpsAldorMakeDirectoryOption;
import aldor.builder.jps.JpsAldorModelSerializerExtension;
import aldor.builder.jps.JpsAldorModuleExtension;
import aldor.builder.jps.JpsAldorModuleType;
import aldor.test_util.AssumptionAware;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileSystemUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.TestLoggerFactory;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.util.TimeoutUtil;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.api.CanceledStatus;
import org.jetbrains.jps.builders.impl.BuildDataPathsImpl;
import org.jetbrains.jps.builders.impl.BuildRootIndexImpl;
import org.jetbrains.jps.builders.impl.BuildTargetIndexImpl;
import org.jetbrains.jps.builders.impl.BuildTargetRegistryImpl;
import org.jetbrains.jps.builders.logging.BuildLoggingManager;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
import org.jetbrains.jps.incremental.BuilderRegistry;
import org.jetbrains.jps.incremental.IncProjectBuilder;
import org.jetbrains.jps.incremental.RebuildRequestedException;
import org.jetbrains.jps.incremental.fs.BuildFSState;
import org.jetbrains.jps.incremental.relativizer.PathRelativizerService;
import org.jetbrains.jps.incremental.storage.BuildDataManager;
import org.jetbrains.jps.incremental.storage.BuildTargetsState;
import org.jetbrains.jps.incremental.storage.ProjectTimestamps;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.indices.impl.IgnoredFileIndexImpl;
import org.jetbrains.jps.indices.impl.ModuleExcludeIndexImpl;
import org.jetbrains.jps.model.JpsDummyElement;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.impl.JavaModuleExtensionRole;
import org.jetbrains.jps.model.library.sdk.JpsSdkReference;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.impl.JpsModuleImpl;
import org.jetbrains.jps.service.JpsServiceManager;
import org.jetbrains.jps.util.JpsPathUtil;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.log4j.Level.DEBUG;

public abstract class AldorJpsTestCase extends AssumptionAware.UsefulTestCase {
    static {
        LogManager.resetConfiguration();
        Appender appender = new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));
        appender.setName("Console");
        LogManager.getRootLogger().addAppender(appender);
        LogManager.getRootLogger().setLevel(DEBUG);
        Logger.setFactory(TestLoggerFactory.class);
    }
    private static final Logger LOG = Logger.getInstance(AldorJpsTestCase.class);

    private File myProjectDir = null;
    private JpsModel myModel;
    private JpsProject myProject;
    private File myDataStorageRoot;
    private TestProjectBuilderLogger myLogger;
    private final Map<String,String> myBuildParams = new HashMap<>();

    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LOG.setLevel(DEBUG);
        myModel = JpsElementFactory.getInstance().createModel();
        myProject = myModel.getProject();
        myDataStorageRoot = FileUtil.createTempDirectory("compile-server-" + getProjectName(), null);
        myLogger = new TestProjectBuilderLogger();
    }

    protected String getProjectName() {
        return StringUtil.decapitalize(StringUtil.trimStart(getName(), "test"));
    }

    protected ProjectDescriptor createProjectDescriptor(final BuildLoggingManager buildLoggingManager) {
        try {
            BuildTargetRegistryImpl targetRegistry = new BuildTargetRegistryImpl(myModel);
            ModuleExcludeIndex index = new ModuleExcludeIndexImpl(myModel);
            IgnoredFileIndexImpl ignoredFileIndex = new IgnoredFileIndexImpl(myModel);
            BuildDataPaths dataPaths = new BuildDataPathsImpl(myDataStorageRoot);
            BuildRootIndexImpl buildRootIndex = new BuildRootIndexImpl(targetRegistry, myModel, index, dataPaths, ignoredFileIndex);
            BuildTargetIndexImpl targetIndex = new BuildTargetIndexImpl(targetRegistry, buildRootIndex);
            BuildTargetsState targetsState = new BuildTargetsState(dataPaths, myModel, buildRootIndex);
            PathRelativizerService relativizer = new PathRelativizerService(myModel.getProject());
            ProjectTimestamps timestamps = new ProjectTimestamps(myDataStorageRoot, targetsState, relativizer);
            BuildDataManager dataManager = new BuildDataManager(dataPaths, targetsState, relativizer);
            return new ProjectDescriptor(myModel, new BuildFSState(true), timestamps, dataManager, buildLoggingManager, index, targetsState,
                    targetIndex, buildRootIndex, ignoredFileIndex);
        } catch (IOException e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
    }

    public File getOrCreateProjectDir() {
        if (myProjectDir == null) {
            try {
                myProjectDir = doGetProjectDir();
            }
            catch (IOException e) {
                //noinspection ProhibitedExceptionThrown
                throw new RuntimeException(e);
            }
        }
        return myProjectDir;
    }

    protected File doGetProjectDir() throws IOException {
        return FileUtil.createTempDirectory("prj", null);
    }

    protected JpsModule addModule(String moduleName) {
        JpsModule module = myProject.addModule(moduleName, JpsAldorModuleType.INSTANCE);
        module.getContentRootsList().addUrl(JpsPathUtil.pathToUrl(new File(getOrCreateProjectDir(), moduleName).toString()));
        return module;
    }

    JpsModule addAldorModule(String moduleName) {
        AldorModuleExtensionProperties properties = new AldorModuleExtensionProperties("aldor-sdk", "out/ao",
                JpsAldorMakeDirectoryOption.Source, AldorModuleExtensionProperties.WithJava.Enabled, "java-sdk");
        JpsSimpleElement<AldorModuleExtensionProperties> simpleElement = JpsElementFactory.getInstance().createSimpleElement(properties);
        JpsModule module = new JpsModuleImpl<>(JpsAldorModuleType.INSTANCE, moduleName, simpleElement);
        myProject.addModule(module);
        module.getContentRootsList().addUrl(JpsPathUtil.pathToUrl(new File(getOrCreateProjectDir(), moduleName).toString()));
        return module;
    }

    private JpsModule addLocalAldorModule(String moduleName, String outputDirectoryName) {
        AldorModuleExtensionProperties data = new AldorModuleExtensionProperties("aldor-sdk", outputDirectoryName, JpsAldorMakeDirectoryOption.BuildRelative, AldorModuleExtensionProperties.WithJava.Enabled, "java-sdk");
        JpsSimpleElement<AldorModuleExtensionProperties> simpleElement = JpsElementFactory.getInstance().createSimpleElement(data);
        JpsModule module = new JpsModuleImpl<>(JpsAldorModuleType.INSTANCE, moduleName, simpleElement);
        myProject.addModule(module);
        module.getContentRootsList().addUrl(JpsPathUtil.pathToUrl(new File(getOrCreateProjectDir(), moduleName).toString()));

        return module;
    }


    protected BuildResult doBuild(final ProjectDescriptor descriptor, CompileScopeTestBuilder scopeBuilder) {
        IncProjectBuilder builder = new IncProjectBuilder(descriptor, BuilderRegistry.getInstance(), myBuildParams, CanceledStatus.NULL, null, true);
        BuildResult result = new BuildResult();
        builder.addMessageHandler(result);
        try {
            beforeBuildStarted(descriptor);
            builder.build(scopeBuilder.build(), false);
            result.storeMappingsDump(descriptor);
        }
        catch (RebuildRequestedException | IOException e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Called as a build is starting
     * @param descriptor The project
     */
    @SuppressWarnings({"EmptyMethod"})
    protected void beforeBuildStarted(@NotNull ProjectDescriptor descriptor) {
        LOG.info("Staring build ");
    }

    protected BuildResult rebuildAllAndSucceed() {
        BuildResult res = doBuild(CompileScopeTestBuilder.rebuild().all());
        res.assertSuccessful();
        return res;
    }

    protected BuildResult rebuildAllAndFail() {
        BuildResult res = doBuild(CompileScopeTestBuilder.rebuild().all());
        res.assertFailed();
        return res;
    }


    protected BuildResult makeAll() {
        return doBuild(CompileScopeTestBuilder.make().all());
    }

    protected BuildResult doBuild(CompileScopeTestBuilder scope) {
        ProjectDescriptor descriptor = createProjectDescriptor(new BuildLoggingManager(myLogger));
        try {
            myLogger.clearFilesData();
            return doBuild(descriptor, scope);
        }
        finally {
            descriptor.release();
        }
    }

    protected void clearBuildLog() {
        myLogger.clearLog();
    }

    public void assertCompiled(String builderName, String... paths) {
        myLogger.assertCompiled(builderName, new File[]{myProjectDir, myDataStorageRoot}, paths);
    }

    protected String createFile(String relativePath) {
        return createFile(relativePath, "");
    }

    protected String createDir(String relativePath) {
        File dir = fileForProjectPath(relativePath);
        boolean created = dir.mkdirs();
        if (!created && !dir.isDirectory()) {
            Assert.fail("Cannot create " + dir.getAbsolutePath() + " directory");
        }
        return FileUtil.toSystemIndependentName(dir.getAbsolutePath());
    }

    public String createFile(String relativePath, final String text) {
        try {
            File file = fileForProjectPath(relativePath);
            FileUtil.writeToFile(file, text);
            return FileUtil.toSystemIndependentName(file.getAbsolutePath());
        }
        catch (IOException e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
    }

    protected void change(String filePath) {
        change(filePath, null);
    }

    protected void change(String relativePath, @Nullable final String newContent) {
        try {
            File file = fileForProjectPath(relativePath);
            Assert.assertTrue("File " + file.getAbsolutePath() + " doesn't exist", file.exists());
            if (newContent != null) {
                FileUtil.writeToFile(file, newContent);
            }
            long oldTimestamp = FileSystemUtil.lastModified(file);
            long time = System.currentTimeMillis();
            setLastModified(file, time);
            if (FileSystemUtil.lastModified(file) <= oldTimestamp) {
                setLastModified(file, time + 1);
                long newTimeStamp = FileSystemUtil.lastModified(file);
                if (newTimeStamp <= oldTimestamp) {
                    //Mac OS and some versions of Linux truncates timestamp to nearest second
                    setLastModified(file, time + 1000);
                    newTimeStamp = FileSystemUtil.lastModified(file);
                    Assert.assertTrue("Failed to change timestamp for " + file.getAbsolutePath(), newTimeStamp > oldTimestamp);
                }
                sleepUntil(newTimeStamp);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void sleepUntil(long time) {
        //we need this to ensure that the file won't be treated as changed by user during compilation and therefore marked for recompilation
        long delta;
        while ((delta = time - System.currentTimeMillis()) > 0) {
            TimeoutUtil.sleep(delta);
        }
    }

    private static void setLastModified(File file, long time) {
        boolean updated = file.setLastModified(time);
        Assert.assertTrue("Cannot modify timestamp for " + file.getAbsolutePath(), updated);
    }


    public File fileForProjectPath(String relativePath) {
        return new File(getOrCreateProjectDir(), relativePath);
    }

    public class AldorLocalFixture {
        private String sourceDirectoryName = "aldor";
        private String moduleName = "aldor-codebase";
        private String outputDirectoryName = "build";
        private String sdkName = "Local";
        public void sourceDirectoryName(String name) {
            this.sourceDirectoryName = name;
        }

        public AldorLocalFixture() {
        }

        public JpsModule createModule() {
            myProject.getModel().getGlobal().addSdk(sdkName, "", "1.0", JpsAldorModelSerializerExtension.JpsAldorSdkType.LOCAL);

            JpsModule module = addLocalAldorModule(moduleName, outputDirectoryName);
            Assert.assertNotNull(module);
            File basePath = new File(getOrCreateProjectDir(), moduleName);
            module.addSourceRoot(JpsPathUtil.pathToUrl(new File(basePath, sourceDirectoryName).getAbsolutePath()), JavaSourceRootType.SOURCE);
            File outputDirectory = new File(basePath, outputDirectoryName);

            module.getContainer().getOrSetChild(JavaModuleExtensionRole.INSTANCE).setOutputUrl(JpsPathUtil.pathToUrl("java-build-directory"));
            JpsSdkReference<JpsDummyElement> sdkReference = JpsElementFactory.getInstance().createSdkReference(sdkName, JpsAldorModelSerializerExtension.JpsAldorSdkType.LOCAL);
            module.getSdkReferencesTable().setSdkReference(JpsAldorModelSerializerExtension.JpsAldorSdkType.LOCAL, sdkReference);

            // This is wrong for a local aldor git clone
            module.getContainer().setChild(JpsAldorModuleExtension.ROLE,
                    new JpsAldorModuleExtension(AldorModuleExtensionProperties.builder().setOutputDirectory(outputDirectoryName).setOption(JpsAldorMakeDirectoryOption.BuildRelative).build()));

            return module;
        }
    }

    public class AldorInstalledFixture {
        private String sourceDirectoryName = "aldor";
        private String moduleName = "aldor-test";
        private String outputDirectoryName = "out/ao";
        private String sdkName = "Local";

        private final JpsElementFactory elementFactory;

        public AldorInstalledFixture() {
            elementFactory = JpsServiceManager.getInstance().getExtensions(JpsElementFactory.class).iterator().next();
        }

        public JpsModule createModule() {
            JpsModule module = addAldorModule("aldor-module");

            Assert.assertNotNull(module);
            module.setName("AldorModule");
            module.getContentRootsList().addUrl("file:///"+ getOrCreateProjectDir());

            module.addSourceRoot("file:///"+ getOrCreateProjectDir(), AldorSourceRootType.INSTANCE);
            module.getSdkReferencesTable().setSdkReference(JpsAldorModelSerializerExtension.JpsAldorSdkType.LOCAL,
                                                          elementFactory.createSdkReference("Local",
                                                                                    JpsAldorModelSerializerExtension.JpsAldorSdkType.LOCAL));
            module.getContainer().setChild(JpsAldorModuleExtension.ROLE,
                    new JpsAldorModuleExtension(AldorModuleExtensionProperties.builder()
                            .setOutputDirectory(outputDirectoryName)
                            .setOption(JpsAldorMakeDirectoryOption.Source)
                            .build()));

            return module;
        }

    }


}
