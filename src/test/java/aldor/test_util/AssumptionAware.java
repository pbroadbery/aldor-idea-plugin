package aldor.test_util;

import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.ModuleFixture;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.junit.AssumptionViolatedException;

public final class AssumptionAware {

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
        protected void runTestRunnable(@NotNull ThrowableRunnable<Throwable> testRunnable) throws Throwable {
            runAware(() -> super.runTestRunnable(testRunnable));
        }

        @Override
        protected boolean shouldRunTest() {
            return super.shouldRunTest() && !assumptionViolated;
        }

        @Override
        protected void tearDown() throws Exception {
            if (!assumptionViolated) {
                super.tearDown();
            }
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

    }

    private interface WildRunnable {
        void run() throws Throwable;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    public abstract static class UsefulTestCase extends com.intellij.testFramework.UsefulTestCase {

        @Override
        protected void runBare(@NotNull ThrowableRunnable<Throwable> testRunnable) throws Throwable {
            super.runBare(() -> runAware(testRunnable::run));
        }
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    public abstract static class HeavyPlatformTestCase extends com.intellij.testFramework.HeavyPlatformTestCase {
        @Override
        protected void runBare(@NotNull ThrowableRunnable<Throwable> testRunnable) throws Throwable {
            super.runBare(() -> runAware(testRunnable::run));
        }
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    public abstract static class NewProjectWizardTestCase extends com.intellij.ide.projectWizard.NewProjectWizardTestCase {
        @Override
        protected void runBare(@NotNull ThrowableRunnable<Throwable> testRunnable) throws Throwable {
            super.runBare(() -> runAware(testRunnable::run));
        }
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @Deprecated
    public abstract static class LightPlatformCodeInsightFixtureTestCase extends com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase {
        @Override
        protected void runBare(@NotNull ThrowableRunnable<Throwable> testRunnable) throws Throwable {
            super.runBare(() -> runAware(testRunnable::run));
        }
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    public abstract static class LightPlatformCodeInsightTestCase extends com.intellij.testFramework.LightPlatformCodeInsightTestCase {
        @Override
        protected void runBare(@NotNull ThrowableRunnable<Throwable> testRunnable) throws Throwable {
            super.runBare(() -> runAware(testRunnable::run));
        }
    }

    @SuppressWarnings({"ClassNameSameAsAncestorName"})
    public abstract static class LightPlatformTestCase extends com.intellij.testFramework.LightPlatformTestCase {
        @Override
        protected void runBare(@NotNull ThrowableRunnable<Throwable> testRunnable) throws Throwable {
            super.runBare(() -> runAware(testRunnable::run));
        }
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    public abstract static class CodeInsightFixtureTestCase<X extends ModuleFixture, T extends ModuleFixtureBuilder<X>> extends com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase<T> {
        @Override
        protected void runTestRunnable(@NotNull ThrowableRunnable<Throwable> testRunnable) throws Throwable {
            super.runTestRunnable(() -> runAware(testRunnable::run));
        }
    }
}
