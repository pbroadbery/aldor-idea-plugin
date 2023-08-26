package aldor.aldorunit.runner;

import foamj.Clos;
import foamj.FoamContext;
import foamj.FoamHelper;
import org.junit.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AldorUnitRunner extends Runner implements Filterable {
    private final Class<?> clzz;
    private FoamContext context;
    private String implementation;
    private final Object childrenLock = new Object();

    // Guarded by childrenLock
    private volatile Collection<AldorUnitRunner.AldorTestRunner> filteredChildren = null;
    private volatile RunnerScheduler scheduler = new RunnerScheduler() {
        @Override
        public void schedule(Runnable childStatement) {
            childStatement.run();
        }

        @Override
        public void finished() {
            // do nothing
        }
    };

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass The test class
     */
    public AldorUnitRunner(Class<?> testClass) throws InitializationError {
        String generatedTestClass = System.getProperty("aldor.aldorunit.testClass");
        try {
            this.clzz = Thread.currentThread().getContextClassLoader().loadClass(generatedTestClass);
        } catch (ClassNotFoundException e) {
            throw new InitializationError("Missing test class " + generatedTestClass);
        }
        this.implementation = System.getProperty("aldor.aldorunit.implementation");
        if (implementation == null) {
            throw new InitializationError("Missing implementation class - aldor.aldorunit.implementation");
        }
        load();
    }

    @Override
    public void filter(Filter filter) {

    }

    protected List<AldorTestRunner> getChildren() {
        List<Method> methods = new ReflectionHelper().findTestMethods(clzz);
        return methods.stream().map(AldorTestRunner::new).collect(Collectors.toList());
    }

    protected Description describeChild(AldorTestRunner child) {
        return child.getDescription();
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    protected void runChild(AldorTestRunner child, RunNotifier runNotifier) {
        runNotifier.fireTestStarted(child.getDescription());
        System.out.println("Starting test " + child.getDescription().getMethodName());
        try {
            child.run(runNotifier);
        } catch (Throwable e) {
            runNotifier.fireTestFailure(new Failure(child.getDescription(), e));
        } finally {
            runNotifier.fireTestFinished(child.getDescription());
            System.out.println("Finished test " + child.getDescription().getMethodName());
        }

    }

    private void load() {
        FoamContext context = new FoamContext();
        FoamHelper.setContext(context);
        Clos fn = context.createLoadFn(implementation);
        fn.call();
        this.context = context;
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(clzz);
        for (AldorUnitRunner.AldorTestRunner child : getFilteredChildren()) {
            description.addChild(describeChild(child));
        }

        return description;
    }

    private Collection<AldorUnitRunner.AldorTestRunner> getFilteredChildren() {
        if (filteredChildren == null) {
            synchronized (childrenLock) {
                if (filteredChildren == null) {
                    filteredChildren = Collections.unmodifiableCollection(getChildren());
                }
            }
        }
        return filteredChildren;
    }


    @Override
    public void run(RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier,
                getDescription());
        try {
            Statement statement = classBlock(notifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.addFailedAssumption(e);
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        }

    }

    protected Statement classBlock(final RunNotifier notifier) {
        Statement statement = childrenInvoker(notifier);
        /*
        if (!areAllChildrenIgnored()) {
            statement = withBeforeClasses(statement);
            statement = withAfterClasses(statement);
            statement = withClassRules(statement);
        }
        */
        return statement;
    }

    private boolean areAllChildrenIgnored() {
        for (AldorTestRunner child : getFilteredChildren()) {
            if (!isIgnored(child)) {
                return false;
            }
        }
        return true;
    }

    // In case we want to ignore things
    protected boolean isIgnored(AldorTestRunner child) {
        return false;
    }


    /**
     * Returns a {@link Statement}: Call {@link #runChild(AldorTestRunner, RunNotifier)}
     * on each object returned by {@link #getChildren()} (subject to any imposed
     * filter and sort)
     */
    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() {
                runChildren(notifier);
            }
        };
    }


    private void runChildren(final RunNotifier notifier) {
        final RunnerScheduler currentScheduler = scheduler;
        try {
            for (final AldorTestRunner each : getFilteredChildren()) {
                currentScheduler.schedule(() -> runChild(each, notifier));
            }
        } finally {
            currentScheduler.finished();
        }
    }



    public class AldorTestRunner {
        private final Method method;

        AldorTestRunner(Method method) {
            this.method = method;
        }

        public Description getDescription() {
            return Description.createTestDescription(clzz, method.getName());
        }

        @SuppressWarnings("OverlyBroadThrowsClause")
        public void run(RunNotifier notifier) throws Throwable {
            try {
                method.invoke(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    static class ReflectionHelper {

        public List<Method> findTestMethods(Class clss) {
            return Arrays.stream(clss.getMethods()).filter(this::isTest).collect(Collectors.toList());
        }

        private boolean isTest(Method m) {
            if (!m.getName().startsWith("test")) {
                return false;
            }
            if (m.getParameterCount() != 0) {
                return false;
            }
            if (!m.getReturnType().equals(Void.TYPE)) {
                return false;
            }
            return true;
        }
    }

}
