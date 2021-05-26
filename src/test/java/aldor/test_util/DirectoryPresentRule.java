package aldor.test_util;

import org.junit.Assume;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;

//@deprecated Use executable present rule
@Deprecated
public class DirectoryPresentRule implements PathBasedTestRule {
    private final String path;

    public DirectoryPresentRule(String path) {
        this.path = path;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        Assume.assumeTrue(shouldRunTest());
        return statement;
    }

    @Override
    public String path() {
        return path;
    }

    public boolean isPresent() {
        File dir = new File(path);
        return dir.isDirectory();
    }

    @Override
    public boolean shouldRunTest() {
        return isPresent();
    }
}
