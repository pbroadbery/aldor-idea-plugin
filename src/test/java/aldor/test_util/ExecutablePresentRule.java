package aldor.test_util;

import org.junit.Assume;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * JUnit rule which will ignore all tests where an aldor executable is not in a number of places:
 * 1) PATH
 * 2) /home/pab/Work/aldorgit/opt/bin/aldor
 * 3) Maybe some other places...
 * Explicit places beat PATH lookup.
 */
public class ExecutablePresentRule implements PathBasedTestRule {
    private final String executableName;
    private final List<String> places;

    public ExecutablePresentRule(String name) {
        this(Collections.emptyList(), name);
    }
    public ExecutablePresentRule(List<String> places, String name) {
        this.places = new ArrayList<>(places);
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
        return executable.get();
    }

    @Override
    public String path() {
        return prefix();
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

    public String prefix() {
        File file = executable();
        if ("bin".equals(file.getParentFile().getName())) {
            return file.getParentFile().getParentFile().getPath();
        }
        throw new RuntimeException("No prefix for executable");
    }

    @SuppressWarnings("serial")
    public static final class MissingExecutableException extends RuntimeException {
        MissingExecutableException(String msg) {
            super(msg);
        }
    }


    public static class AldorStd extends ExecutablePresentRule {
        public static final ExecutablePresentRule INSTANCE = new AldorStd();

        public AldorStd() {
            super(Collections.singletonList("/home/pab/Work/aldorgit/opt/bin"), "aldor");
        }
    }

    public static class AldorDev extends ExecutablePresentRule {
        public static final ExecutablePresentRule INSTANCE = new AldorDev();

        public AldorDev() {
            super(Collections.singletonList("/home/pab/Work/aldorgit/utypes/opt/bin"), "aldor");
        }
    }

    public static class Aldor extends ExecutablePresentRule {
        public static final ExecutablePresentRule INSTANCE = new Aldor();
        public Aldor() {
            super(Collections.singletonList("/home/pab/Work/IdeaProjects/aldor-codebase/opt/bin"), "aldor");
        }
    }

    public static class Fricas extends ExecutablePresentRule {
        public static final ExecutablePresentRule INSTANCE = new Fricas();
        private static final String basePath = "/home/pab/IdeaProjects/fricas-codebase/opt/lib/fricas/target";

        public Fricas() {
            super(Arrays.asList(basePath + "/x86_64-unknown-linux/bin", basePath + "/x86_64-linux-gnu/bin"), "fricas");
        }

    }

    /**
     * For JUnit3/LightPlatformTestCase
     * @return true if test is runnable
     */
    @Override
    public boolean shouldRunTest() {
        return findExecutable().isPresent();
    }
}
