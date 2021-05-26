package aldor.test_util;

import org.junit.rules.TestRule;

public interface PathBasedTestRule extends TestRule {
    boolean shouldRunTest();
    String path();

}
