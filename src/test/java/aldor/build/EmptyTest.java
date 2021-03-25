package aldor.build;

import aldor.test_util.AssumptionAware;
import junit.framework.TestCase;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.Test;

public class EmptyTest extends AssumptionAware.TestCase {
    public void test() {
        Assume.assumeFalse(true);
    }

    @Override
    public void runBare() throws Throwable {
        try {
            super.runBare();
        }
        catch (AssumptionViolatedException e) {

        }
    }
}
