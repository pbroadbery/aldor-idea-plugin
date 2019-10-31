package aldor.builder.files;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static aldor.util.StringUtilsAldorRt.trimExtension;

/**
 * Utilities for build files (Jps style).
 */
public final class BuildFiles {

    @NotNull
    @Contract(pure = true)
    public static String buildTargetName(@NotNull File sourceRoot, @NotNull File file) {
        return "out/ao/" + trimExtension(file.getName()) + ".ao";
    }

    @Nullable
    private static File buildDirectoryFor(File buildRootDirectory, final File directory) {
        // TODO: This should match AldorModuleManager
        if (directory == null) {
            return buildRootDirectory.getParentFile();
        }
        File configScript = new File(directory, "configure.ac");
        if (configScript.exists()) {
            return buildRootDirectory;
        }
        else if (new File(directory, "Makefile").exists()) {
            return directory;
        }
        else {
            File parentBuild = buildDirectoryFor(buildRootDirectory, directory.getParentFile());
            return (parentBuild == null) ? null : new File(parentBuild, directory.getName());
        }
    }

    public static String localBuildTargetName(File sourceRoot, File sourceFile) {
        return trimExtension(sourceFile.getName()) + ".ao";
    }
}
