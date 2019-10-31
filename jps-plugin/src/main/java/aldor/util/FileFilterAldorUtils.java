package aldor.util;

import org.jetbrains.annotations.NotNull;

import java.io.FileFilter;

public final class FileFilterAldorUtils {

    public static FileFilter or(FileFilter filter1, FileFilter filter2) {
        return file -> filter1.accept(file) || filter2.accept(file);
    }

    public static FileFilter nameFileFilter(@NotNull String name) {
        return file -> file.getName().equals(name);
    }
}
