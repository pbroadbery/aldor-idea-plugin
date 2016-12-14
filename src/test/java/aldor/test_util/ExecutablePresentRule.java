package aldor.test_util;

import com.google.common.collect.Lists;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * JUnit rule which will ignore all tests where an aldor executable is not in a number of places:
 * 1) PATH
 * 2) /home/pab/Work/aldorgit/opt/bin/aldor
 * 3) Maybe some other places...
 * Explicit places beat PATH lookup.
 */
public class ExecutablePresentRule implements TestRule {
    static final List<String> places = Lists.newArrayList("/home/pab/IdeaProjects/aldor-codebase/opt/bin");
    private final String executableName;

    public ExecutablePresentRule(String name) {
        this.executableName = name;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        Optional<File> executable = findExecutable();
        Assume.assumeTrue(executable.isPresent());

        return statement;
    }

    public File executable() {
        Optional<File> executable = findExecutable();
        Assume.assumeTrue(executable.isPresent());
        assert executable.isPresent();
        return executable.get();
    }

    private Optional<File> findExecutable() {
        Optional<File> directLookup = lookupPlaces();
        if (directLookup.isPresent()) {
            return directLookup;
        }
        else {
            return lookupPath();
        }
    }

    private Optional<File> lookupPlaces() {
        return places.stream().map(File::new)
                .map(dir -> new File(dir, executableName))
                .filter(File::canExecute)
                .findFirst();
    }

    private Optional<File> lookupPath() {
        //noinspection CallToSystemGetenv
        String[] path = System.getenv("PATH").split(File.pathSeparator);
        for (String pathElement: path) {
            File dir = new File(pathElement);
            File executable = new File(dir, executableName);
            if (executable.canExecute()) {
                return Optional.of(executable);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("serial")
    public static final class MissingExecutableException extends RuntimeException {
        MissingExecutableException(String msg) {
            super(msg);
        }
    }

    public static class Aldor extends ExecutablePresentRule {
        public Aldor() {
            super("aldor");
        }
    }

    /**
     * For JUnit3/LightPlatformTestCase
     * @return
     */
    public boolean shouldRunTest() {
        return findExecutable().isPresent();
    }
}
