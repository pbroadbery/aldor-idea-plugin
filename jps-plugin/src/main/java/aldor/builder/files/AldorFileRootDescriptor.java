package aldor.builder.files;

import aldor.util.FileFilterAldorUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileFilters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildRootDescriptor;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Set;

public class AldorFileRootDescriptor extends BuildRootDescriptor {
    private static final Logger LOG = Logger.getInstance(AldorFileRootDescriptor.class);
    private final File sourceFile;
    private final AldorFileBuildTarget target;
    private final File sourceRoot;

    AldorFileRootDescriptor(AldorFileBuildTarget target, File sourceRoot, File sourceFile) {
        this.sourceFile = sourceFile;
        this.target = target;
        this.sourceRoot = sourceRoot;
    }

    @Override
    public String getRootId() {
        return sourceFile.getAbsolutePath();
    }

    @Override
    public File getRootFile() {
        return sourceRoot;
    }

    @Override
    public AldorFileBuildTarget getTarget() {
        return target;
    }

    @NotNull
    @Override
    public FileFilter createFileFilter() {
        return FileFilterAldorUtils.or(FileFilterAldorUtils.nameFileFilter("Makefile"), FileFilters.filesWithExtension(".as"));
    }

    @NotNull
    @Override
    public Set<File> getExcludedRoots() {
        return Collections.singleton(new File(sourceRoot, "out"));
    }

}
