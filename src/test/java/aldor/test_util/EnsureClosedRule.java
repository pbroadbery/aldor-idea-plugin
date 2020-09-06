package aldor.test_util;

import aldor.util.InstanceCounter;
import com.intellij.openapi.diagnostic.Logger;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.IdentityHashMap;
import java.util.Map;

public class EnsureClosedRule implements TestRule, CloseCheck {
    private static final Logger LOG = Logger.getInstance(EnsureClosedRule.class);
    private final Map<Object, CheckedClosable> items = new IdentityHashMap<>();

    @Override
    public void add(Object cl) {
        this.items.put(cl, new CheckedClosable());
    }

    void check() {
        for (CheckedClosable checkedClosable : items.values()) {
            LOG.warn("failed to close " + checkedClosable);
            checkedClosable.creation.printStackTrace();
        }
        if (!items.isEmpty()) {
            Assert.fail("Failed to close some resources");
        }
    }

    @Override
    public void close(Object cl) {
        items.remove(cl);
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return JUnits.prePostStatement(() -> {}, this::check, statement);
    }

    public class CheckedClosable {
        private final Exception creation = new Exception("here");
        private final int instanceId = InstanceCounter.instance().next(CheckedClosable.class);

        @Override
        public String toString() {
            return "CheckedClosable{" + instanceId + '}';
        }
    }
}
