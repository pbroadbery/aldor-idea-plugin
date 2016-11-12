package aldor.build.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.ModuleFixture;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureBuilderImpl;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureImpl;
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl;

import java.io.IOException;
import java.util.Optional;

public class AldorModuleTest extends UsefulTestCase {

    private Project project;
    private final TempDirTestFixture tempDirTestFixture = new TempDirTestFixtureImpl();

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getName());
        IdeaTestFixtureFactory.getFixtureFactory().registerFixtureBuilder(AldorModuleFixtureBuilder.class, AldorModuleFixtureBuilder.class);
        AldorModuleFixtureBuilder<?> moduleFixtureBuilder = projectBuilder.addModule(AldorModuleFixtureBuilder.class);

        moduleFixtureBuilder.addContentRoot(tempDirTestFixture.getTempDirPath());

        IdeaProjectTestFixture fixture = projectBuilder.getFixture();
        moduleFixtureBuilder.instantiateFixture();
        fixture.setUp();
        project = fixture.getProject();
    }

    public void testModule() throws IOException {

        AldorModuleManager manager = AldorModuleManager.getInstance(project);
        assertNotNull(manager);

        assertEquals(1, manager.aldorModules().size());

        Module module = manager.aldorModules().iterator().next();

        VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();

        assertEquals(1, roots.length);
        @SuppressWarnings({"unused", "UnusedAssignment"})
        VirtualFile configure_ac = createFile("root/aldor/configure.ac", "");
        VirtualFile foo_as = createFile("root/aldor/src/foo.as", "");
        createFile("root/build", "");

        Optional<Module> someModule = manager.aldorModuleForFile(foo_as);
        assertTrue(someModule.isPresent());
        assertSame(module, someModule.get());

        VirtualFile root = ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(foo_as);
        assertNotNull(root);
        String path = manager.buildPathForFile(foo_as);
        assertEquals(root.getPath() + "/build/src", path);

        String annotationFile = manager.annotationFileForSourceFile(foo_as);
        assertEquals(root.getPath() + "/build/src/foo.abn", annotationFile);
    }

    private VirtualFile createFile(String path, String text) throws IOException {
        return tempDirTestFixture.createFile(path, text);
    }

    private VirtualFile createDirectory(String path) throws IOException {
        return tempDirTestFixture.findOrCreateDir(path);
    }

    private static final class AldorModuleFixtureBuilder<T extends ModuleFixture> extends ModuleFixtureBuilderImpl<ModuleFixture> {

        private AldorModuleFixtureBuilder(TestFixtureBuilder<? extends IdeaProjectTestFixture> fixtureBuilder) {
            super(AldorModuleType.instance(), fixtureBuilder);
        }

        @Override
        protected ModuleFixture instantiateFixture() {
            return new ModuleFixtureImpl(this);
        }
    }
}
