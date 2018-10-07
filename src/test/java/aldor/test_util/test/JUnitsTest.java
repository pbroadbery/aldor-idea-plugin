package aldor.test_util.test;

import aldor.test_util.JUnits;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JUnitsTest {

    @Test
    public void testRuleThing() throws Throwable {
        List<String> execution = new ArrayList<>();
        RuleChain rule = RuleChain.emptyRuleChain()
                .around(JUnits.prePostTestRule(() -> execution.add("1"), () -> execution.add("-1")))
                .around(JUnits.prePostTestRule(() -> execution.add("2"), () -> execution.add("-2")));

        Statement stmt2 = rule.apply(new Statement() {
            @Override
            public void evaluate() {
                execution.add("test");
            }
        }, Description.createTestDescription(JUnitsTest.class, "Hello"));
        stmt2.evaluate();
        assertEquals(Arrays.asList("1", "2", "test", "-2", "-1"), execution);
    }

    @Test
    public void testRule2() throws Throwable {
        List<String> execution = new ArrayList<>();
        RuleChain rule = RuleChain.emptyRuleChain()
                .around(JUnits.prePostTestRule(() -> execution.add("1"), () -> execution.add("-1")));

        Statement stmt2 = rule.apply(new Statement() {
            @Override
            public void evaluate() {
                execution.add("vomit");
                throw new SomeException();
            }
        }, Description.createTestDescription(JUnitsTest.class, "Hello"));
        try {
            stmt2.evaluate();
            fail();
        }
        catch (RuntimeException ignored) {

        }
        assertEquals(Arrays.asList("1", "vomit", "-1"), execution);
    }

    @SuppressWarnings("serial")
    private static class SomeException extends RuntimeException {}
}