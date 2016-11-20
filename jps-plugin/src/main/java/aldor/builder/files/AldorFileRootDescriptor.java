package aldor.builder.files;

import aldor.builder.files.AldorFileBuildTargetType.AldorFileBuildTarget;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.stream.Collectors;

public class AldorFileRootDescriptor extends BuildRootDescriptor {
    private static final Logger LOG = Logger.getInstance(AldorFileRootDescriptor.class);
    private final File path;
    private final AldorFileBuildTarget target;
    private final List<File> contentRoots;

    AldorFileRootDescriptor(AldorFileBuildTarget target, @NotNull JpsModule module, File file) {
        this.path = file;
        this.target = target;
        //assert module.getContentRootsList() != null;
        this.contentRoots = module.getContentRootsList().getUrls().stream().map(JpsPathUtil::urlToFile).collect(Collectors.toList());
    }

    @Override
    public String getRootId() {
        return path.getAbsolutePath();
    }

    @Override
    public File getRootFile() {
        return path;
    }

    public File getSourceFile() {
        return path;
    }

    @Override
    public AldorFileBuildTarget getTarget() {
        return target;
    }

    @Nullable
    public File buildDirectoryForFile(File file) {
        return BuildFiles.buildDirectoryForFile(contentRoots, file);
    }

    @NotNull
    @Override
    public FileFilter createFileFilter() {
        return pathname -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Accept: " + this.getRootId() + " " + pathname);
            }
            return FileUtil.filesEqual(path, pathname);
        };
    }
}
