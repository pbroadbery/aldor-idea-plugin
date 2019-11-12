package aldor.test_util;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SkipOnCIBuildRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
       if (description.getAnnotation(SkipCI.class) == null) {
            return statement;
        }

        if (JUnits.isCIBuild()) {
            //noinspection ReturnOfInnerClass
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    throw new AssumptionViolatedException("Not running " + description.getDisplayName() + " under CI");
                }
            };
        } else {
            return statement;
        }

    }
}
