package aldor.test_util;

import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightPlatformTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import org.jetbrains.annotations.NonNls;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;

import static org.junit.Assert.assertFalse;

@SuppressWarnings("ClassWithTooManyDependents")
public class LightPlatformJUnit4TestRule implements TestRule {
    private final String basePath;
    private final CodeInsightTestFixture myFixture;

    public LightPlatformJUnit4TestRule(CodeInsightTestFixture fixture, String basePath) {
        this.myFixture = fixture;
        this.basePath = basePath;
    }

    public void setUp() throws Exception {
        myFixture.setUp();
        myFixture.setTestDataPath(getTestDataPath());
    }

    @NonNls
    protected String getTestDataPath() {
        String path = PlatformTestUtil.getCommunityPath();
        return path.replace(File.separatorChar, '/') + basePath;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return JUnits.prePostStatement(this::setUp, this::tearDown, statement);
    }

    private void tearDown() throws Exception {
        myFixture.tearDown();
        LightPlatformTestCase.reportTestExecutionStatistics();
    }

    public Project getProject() {
        return myFixture.getProject();
    }

    public static CodeInsightTestFixture createFixture(LightProjectDescriptor descriptor) {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder(descriptor);
        //        System.out.println("create fixture; SDK is: " + descriptor.sdk());
        final IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        CodeInsightTestFixture testFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, new LightTempDirTestFixtureImpl(true));

        System.out.println("createFixture - " + testFixture.getProject());
        return testFixture;
    }

    public static CodeInsightTestFixture createHeavyweightFixture(LightProjectDescriptor descriptor) {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createFixtureBuilder("heavy");
        //        System.out.println("create fixture; SDK is: " + descriptor.sdk());
        final IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        CodeInsightTestFixture testFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, new LightTempDirTestFixtureImpl(true));

        System.out.println("createFixture - " + testFixture.getProject());
        return testFixture;
    }


}
