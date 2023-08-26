package aldor.util;

import aldor.test_util.UnsafeRunnable;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.junit.AssumptionViolatedException;

public final class AssumptionAware {

    static void assumptionFailed(AssumptionViolatedException e) {
        System.out.println("** Assumption failed: " + e.getMessage());
        e.printStackTrace();
    }

    static void runAware(@NotNull WildRunnable r) throws Throwable {
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


    public abstract static class TestCase extends junit.framework.TestCase {
        @Override
        public void runBare() throws Throwable {
            runAware(super::runBare);
        }
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
}
