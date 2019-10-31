package aldor.test_util;

import com.intellij.testFramework.EdtTestUtil;
import org.jetbrains.annotations.Nullable;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SwingThreadTestRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        return new RunOnEdtStatement(statement);
    }

    private static final class RunOnEdtStatement extends Statement {
        private final Statement statement;
        @Nullable
        private Throwable exception;

        private RunOnEdtStatement(Statement statement) {
            this.statement = statement;
            exception = null;
        }

        @Override
        public void evaluate() throws Throwable {
            EdtTestUtil.runInEdtAndWait(() -> {
                //noinspection ErrorNotRethrown
                try {
                    statement.evaluate();
                }
                catch (Error | RuntimeException error) {
                    exception = error;
                }
            });
            if (exception != null) {
                throw exception;
            }
        }
    }
}
