package aldor.test_util.test;

import aldor.test_util.ExecutablePresentRule;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ExecutablePresentRuleTest {
    private String result = null;

    @Test
    public void testExecPresentRule() throws Throwable {
        TestRule rule = new ExecutablePresentRule("ls");

        Statement statement = rule.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                result = "executed";
            }
        }, Description.createTestDescription(ExecutablePresentRuleTest.class, "test"));

        statement.evaluate();
        assertEquals("executed", result);
    }

    @Test
    public void testExecPresentRule_missing() throws Throwable {
        TestRule rule = new ExecutablePresentRule(UUID.randomUUID().toString());

        try {
            Statement statement = rule.apply(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    result = "executed";
                }
            }, Description.createTestDescription(ExecutablePresentRuleTest.class, "test"));
            Assert.fail("nope");
        } catch (AssumptionViolatedException ignored) {

        }
        Assert.assertNull(result);
    }
}
