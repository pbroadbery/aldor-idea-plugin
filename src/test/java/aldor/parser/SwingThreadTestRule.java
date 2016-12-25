package aldor.parser;

import com.intellij.testFramework.EdtTestUtil;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SwingThreadTestRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            private Throwable exception = null;
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
        };
    }
}
