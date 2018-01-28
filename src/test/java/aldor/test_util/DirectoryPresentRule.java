package aldor.test_util;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;

public class DirectoryPresentRule implements TestRule {
    private final String path;

    public DirectoryPresentRule(String path) {
        this.path = path;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        Assume.assumeTrue(isPresent());
        return statement;
    }

    public String path() {
        return path;
    }

    public boolean isPresent() {
        File dir = new File(path);
        return dir.isDirectory();
    }
}
