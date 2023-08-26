package aldor.builder.jps.autoconf.descriptors;

import aldor.builder.jps.util.Sx;
import aldor.util.HasSxForm;
import aldor.util.SxForm;
import aldor.util.SxFormUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

import static aldor.util.SxFormUtils.collectList;
import static aldor.util.SxFormUtils.file;
import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;

public class SourceRootPattern implements HasSxForm {
    private final File rootFile;
    private final Sx.FileFilter fileFilter;
    private final Set<File> excludedRoots;
    private final boolean canUseCache;

    public SourceRootPattern(File rootFile, Sx.FileFilter fileFilter, Set<File> excludedRoots, boolean canUseCache) {
        this.rootFile = rootFile;
        this.fileFilter = fileFilter;
        this.excludedRoots = excludedRoots;
        this.canUseCache = canUseCache;
    }

    public FileFilter filter() {
        return fileFilter;
    }

    public Set<File> excludedRoots() {
        return excludedRoots;
    }

    public boolean canUseCache() {
        return canUseCache;
    }

    public File rootFile() {
        return rootFile;
    }

    @Override @NotNull
    public SxForm sxForm() {
        var properties = SxFormUtils.tagged()
                .with("rootFile", file(rootFile))
                .with("useCache", SxFormUtils.bool(canUseCache))
                .with("fileFilter", SxFormUtils.asForm(fileFilter));
        if (!excludedRoots.isEmpty()) {
            properties = properties.with("ExcludedRoots", excludedRoots.stream().map(file -> file(file)).collect(collectList()));
        }
        return list().add(name("SourceRootPattern")).add(properties);
    }
}
