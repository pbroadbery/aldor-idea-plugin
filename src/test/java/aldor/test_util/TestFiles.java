package aldor.test_util;

import org.jetbrains.annotations.NotNull;
import org.junit.Assume;

import java.io.File;

public final class TestFiles {
    @NotNull
    public static File existingFile(String pathname) {
        File file = new File(pathname);
        Assume.assumeTrue("File does not exist: " + pathname, file.exists());
        return file;
    }

}
