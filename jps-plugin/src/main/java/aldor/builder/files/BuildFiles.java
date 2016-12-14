package aldor.builder.files;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

/**
 * Utilities for build files (Jps style).
 */
public final class BuildFiles {

    @Nullable
    public static File buildDirectoryForFile(Collection<File> contentRoots, File file) {
        Optional<File> maybeRoot = contentRoots.stream().filter(root -> FileUtil.isAncestor(root, file, false)).findFirst();
        if (!maybeRoot.isPresent()) {
            return null;
        }
        File theRoot = maybeRoot.get();
        File buildRootDirectory = new File(theRoot, "build");
        return buildDirectoryFor(buildRootDirectory, file.getParentFile());
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


}
