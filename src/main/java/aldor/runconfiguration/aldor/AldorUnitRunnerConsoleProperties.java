package aldor.runconfiguration.aldor;

import com.intellij.execution.Executor;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.ui.ComponentContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AldorUnitRunnerConsoleProperties extends SMTRunnerConsoleProperties {

    public AldorUnitRunnerConsoleProperties(AldorUnitConfiguration configuration, Executor executor) {
        super(configuration, "AldorUnit", executor);
    }

    @Nullable
    @Override
    public AbstractRerunFailedTestsAction createRerunFailedTestsAction(ConsoleView consoleView) {
        return new AldorRerunFailedTestsAction(consoleView, this);
    }

    private class AldorRerunFailedTestsAction extends AbstractRerunFailedTestsAction {
        protected AldorRerunFailedTestsAction(@NotNull ComponentContainer componentContainer, TestConsoleProperties consoleProperties) {
            super(componentContainer);
            init(consoleProperties);

        }
    }
}
