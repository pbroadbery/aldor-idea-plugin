package aldor.test_util;

import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

public final class JUnitsBase {

    public static TestRule prePostTestRule(UnsafeRunnable pre, UnsafeRunnable post) {
        return (statement, description) -> prePostStatement(pre, post, statement);
    }

    public static Statement prePostStatement(UnsafeRunnable pre, UnsafeRunnable post, Statement statement) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                pre.run();
                Throwable fail = null;
                try {
                    statement.evaluate();
                } finally {
                    try {
                        post.run();
                    } catch (RuntimeException e) {
                        System.out.println("Exception at end of rule " + e.getMessage());
                        fail = e;
                    }
                }
                if (fail != null) {
                    throw fail;
                }
            }
        };
    }

}
