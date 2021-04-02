package aldor.test_util;

import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.ModuleFixture;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.junit.AssumptionViolatedException;

public class AssumptionAware {

    static void assumptionFailed(AssumptionViolatedException e) {
        System.out.println("** Assumption failed: " + e.getMessage());
        e.printStackTrace();
    }

    static void runAware(@NotNull WildRunnable r) throws Throwable{
        try {
            r.run();
        } catch (AssumptionViolatedException e) {
            assumptionFailed(e);
        }
    }

    static void runAwareException(@NotNull UnsafeRunnable r) throws Exception {
        try {
            r.run();
        } catch (AssumptionViolatedException e) {
            assumptionFailed(e);
        }
    }

    @SuppressWarnings("ALL")
    public abstract static class BasePlatformTestCase extends com.intellij.testFramework.fixtures.BasePlatformTestCase {
        boolean assumptionViolated  = false;
        @Override
        protected void setUp() throws Exception {
            try {
                super.setUp();
            }
            catch (AssumptionViolatedException e) {
                System.out.println("Assumption violated "+ e.getMessage());
                e.printStackTrace();
                assumptionViolated = true;
            }
        }

        @Override
        protected void runTest() throws Throwable {
            if (!assumptionViolated) {
                runAware(() -> super.runTest());
            }
        }

        @Override
        protected void tearDown() throws Exception {
            if (!assumptionViolated) {
                super.tearDown();
            }
        }

        @Override
        public void runBare() throws Throwable {
            runAware(super::runBare);
        }
    }

    @SuppressWarnings("ALL")
    public abstract static class TestCase extends junit.framework.TestCase {
        @Override
        public void runBare() throws Throwable {
            runAware(super::runBare);
        }
    }

    @SuppressWarnings("ALL")
    public abstract static class LightIdeaTestCase extends com.intellij.testFramework.LightIdeaTestCase {
        @Override
        public void runBareImpl(ThrowableRunnable<?> start) throws Throwable {
            runAware(() -> super.runBareImpl(start));
        }
    }

    private interface WildRunnable {
        void run() throws Throwable;
    }

    public abstract static class UsefulTestCase extends com.intellij.testFramework.UsefulTestCase {

        @Override
        public void runBare() throws Throwable {
            runAware(super::runBare);
        }
    }

    public abstract static class HeavyPlatformTestCase extends com.intellij.testFramework.HeavyPlatformTestCase {
        @Override
        public void runBare() throws Throwable {
            runAware(super::runBare);
        }
    }

    public abstract static class NewProjectWizardTestCase extends com.intellij.ide.projectWizard.NewProjectWizardTestCase {
        @Override
        public void runBare() throws Throwable {
            runAware(super::runBare);
        }
    }

    @Deprecated
    public abstract static class LightPlatformCodeInsightFixtureTestCase extends com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase {
        @Override
        public void runBare() throws Throwable {
            runAware(super::runBare);
        }
    }

    public abstract static class LightPlatformCodeInsightTestCase extends com.intellij.testFramework.LightPlatformCodeInsightTestCase {
        @Override
        public void runBareImpl(ThrowableRunnable<?> start) throws Throwable {
            runAware(() -> super.runBareImpl(start));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public abstract static class LightPlatformTestCase extends com.intellij.testFramework.LightPlatformTestCase {
        @Override
        public void runBareImpl(ThrowableRunnable<?> start) throws Throwable {
            runAware(() -> super.runBareImpl(start));
        }
    }

    public abstract static class CodeInsightFixtureTestCase<X extends ModuleFixture, T extends ModuleFixtureBuilder<X>> extends com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase<T> {
        @Override
        public void defaultRunBare() throws Throwable {
            runAware(super::defaultRunBare);
        }
    }
}
