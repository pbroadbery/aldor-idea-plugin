package aldor.module.template;

import aldor.test_util.JUnits;
import com.google.common.io.Files;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.File;

import static org.junit.Assert.assertNotNull;

public class AldorSimpleModuleBuilderTest {
    private final IdeaProjectTestFixture fixture = IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder().getFixture();

    @Rule
    public TestRule testRule = RuleChain.emptyRuleChain()
            .around(JUnits.swingThreadTestRule())
            .around(JUnits.fixtureRule(fixture))
            .around(JUnits.setLogToDebugTestRule)
            ;

    @Test
    public void testBuilder() throws Exception {
        ModifiableModuleModel model = ModuleManager.getInstance(fixture.getProject()).getModifiableModel();
        //model.newModule("/tmp", "Aldor");
        File tmp = Files.createTempDir();
        try {
            AldorSimpleModuleBuilder builder = new AldorSimpleModuleBuilder();
            builder.setContentEntryPath(tmp.getPath());
            builder.setName("Simple");
            builder.setModuleFilePath(tmp.getPath());
            Module module = ApplicationManager.getApplication().runWriteAction((ThrowableComputable<Module, Exception>) () -> builder.createModule(model));
            assertNotNull(module);
        }
        finally {
            if (!tmp.delete()) {
                Assert.fail();
            }
        }
    }

}