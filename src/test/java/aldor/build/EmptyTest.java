package aldor.build;

import aldor.test_util.AssumptionAware;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;

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
            System.out.println("Assumption failed, as expected");
        }
    }
}
