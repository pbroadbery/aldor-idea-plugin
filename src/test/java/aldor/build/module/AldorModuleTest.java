package aldor.build.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.ModuleFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureBuilderImpl;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureImpl;

import java.util.Arrays;

/**
 * Created by pab on 29/09/16.
 */
public class AldorModuleTest extends UsefulTestCase {

    private Project project;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getName());
        IdeaTestFixtureFactory.getFixtureFactory().registerFixtureBuilder(AldorModuleFixtureBuilder.class, AldorModuleFixtureBuilder.class);
        AldorModuleFixtureBuilder<?> moduleFixtureBuilder = projectBuilder.addModule(AldorModuleFixtureBuilder.class);
        IdeaProjectTestFixture fixture = projectBuilder.getFixture();
        moduleFixtureBuilder.instantiateFixture();
        fixture.setUp();
        project = fixture.getProject();
    }

    public void testModule() {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        System.out.println("Modules: " + Arrays.asList(modules));

        Module m = modules[0];
    }


    private static final class AldorModuleFixtureBuilder<T extends ModuleFixture>  extends ModuleFixtureBuilderImpl<ModuleFixture> {

        private AldorModuleFixtureBuilder(TestFixtureBuilder<? extends IdeaProjectTestFixture>  fixtureBuilder) {
            super(AldorModuleType.instance(), fixtureBuilder);
        }

        @Override
        protected ModuleFixture instantiateFixture() {
            return new ModuleFixtureImpl(this);
        }
    }
}
