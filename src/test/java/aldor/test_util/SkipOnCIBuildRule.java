package aldor.test_util;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Objects;

public class SkipOnCIBuildRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
       if (description.getAnnotation(SkipCI.class) == null) {
            return statement;
        }

        //noinspection AccessOfSystemProperties
        if (!Objects.equals(System.getProperty("aldor.build.skip_ci"), "true")) {
            return statement;
        } else {
            //noinspection InnerClassTooDeeplyNested,ReturnOfInnerClass
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    throw new AssumptionViolatedException("Not running some tests under CI");
                }
            };
        }

    }
}
