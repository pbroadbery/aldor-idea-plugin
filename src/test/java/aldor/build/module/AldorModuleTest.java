package aldor.build.module;

import aldor.build.facet.aldor.AldorFacet;
import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.module.AldorFacetProperties;
import aldor.builder.jps.module.MakeConvention;
import aldor.test_util.AssumptionAware;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.ModuleFixture;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureBuilderImpl;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureImpl;
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.io.IOException;

public class AldorModuleTest extends AssumptionAware.UsefulTestCase {
    private final TempDirTestFixture tempDirTestFixture = new TempDirTestFixtureImpl();
    private IdeaProjectTestFixture fixture;

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getName());
        IdeaTestFixtureFactory.getFixtureFactory().registerFixtureBuilder(AldorModuleFixtureBuilder.class, AldorModuleFixtureBuilder.class);
        AldorModuleFixtureBuilder<?> moduleFixtureBuilder = projectBuilder.addModule(AldorModuleFixtureBuilder.class);

        moduleFixtureBuilder.addContentRoot(tempDirTestFixture.getTempDirPath());

        fixture = projectBuilder.getFixture();
        moduleFixtureBuilder.instantiateFixture();
        fixture.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            if (fixture != null) {
                fixture.tearDown();
            }
        }
        finally {
            super.tearDown();
        }
    }

    public void testModule2() throws IOException {
        testModule();
    }
    public void testModule() throws IOException {

        AldorModuleManager manager = AldorModuleManager.getInstance(fixture.getProject());
        Assert.assertNotNull(manager);

        Assert.assertEquals(1, manager.aldorModules(fixture.getProject()).size());

        Module module = manager.aldorModules(fixture.getProject()).iterator().next();

        VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();

        Assert.assertEquals(1, roots.length);
        //@SuppressWarnings({"UnusedAssignment"})
        VirtualFile foo_as = createFile("root/aldor/src/foo.as", "");
        createFile("root/build", "");

        VirtualFile root = ProjectRootManager.getInstance(fixture.getProject()).getFileIndex().getContentRootForFile(foo_as);
        Assert.assertNotNull(root);
        String path = manager.buildPathForFile(fixture.getProject(), foo_as);
        // FIXME: Not clear what build plan should be
        Assert.assertEquals(root.getPath() + "/root/aldor/src", path);

        String annotationFile = manager.annotationFileForSourceFile(fixture.getProject(), foo_as);
        Assert.assertEquals(root.getPath() + "/root/aldor/src/foo.abn", annotationFile);
    }

    private VirtualFile createFile(String path, String text) throws IOException {
        return tempDirTestFixture.createFile(path, text);
    }

    private VirtualFile createDirectory(String path) throws IOException {
        return tempDirTestFixture.findOrCreateDir(path);
    }

    private static final class AldorModuleFixtureBuilder<T extends ModuleFixture> extends ModuleFixtureBuilderImpl<ModuleFixture> {

        private AldorModuleFixtureBuilder(TestFixtureBuilder<? extends IdeaProjectTestFixture> fixtureBuilder) {
            super(new AldorModuleType(), fixtureBuilder);
        }

        @Override
        protected ModuleFixture instantiateFixture() {
            return new ModuleFixtureImpl(this);
        }

        @Override
        protected @NotNull Module createModule() {
            Module module = super.createModule();
            // I think this is broken... Need to test mapping of source to build directories properly
            AldorFacet.createFacetIfMissing(module, new AldorFacetProperties().asBuilder()
                            .makeConvention(MakeConvention.Source)
                            .outputDirectory("build")
                    .build());
            return module;
        }

        @Override
        protected void setupRootModel(ModifiableRootModel rootModel) {
            ContentEntry ent = rootModel.getContentEntries()[0];
            ent.addSourceFolder(ent.getFile(), AldorSourceRootType.INSTANCE);
        }
    }
}
