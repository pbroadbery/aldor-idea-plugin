package aldor.builder.jps.autoconf;

import aldor.builder.jps.autoconf.descriptors.SourceRootPattern;
import aldor.builder.jps.util.Sx;
import aldor.util.SxForm;
import aldor.util.SxFormUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;

class SimpleSourceRoot extends Sx.BuildRootDescriptor {
    private final Sx.BuildTarget<?> target;
    private final String id;
    private final SourceRootPattern sourceRootPattern;

    SimpleSourceRoot(Sx.BuildTarget<?> target, String id, SourceRootPattern sourceRootPattern) {
        this.target = target;
        this.id = id;
        this.sourceRootPattern = sourceRootPattern;
    }

    @Override @NotNull
    public FileFilter createFileFilter() {
        return sourceRootPattern.filter();
    }

    @Override @NotNull
    public Set<File> getExcludedRoots() {
        return sourceRootPattern.excludedRoots();
    }

    @Override
    public boolean canUseFileCache() {
        return sourceRootPattern.canUseCache();
    }

    @Override
    public String getRootId() {
        return id;
    }

    @Override
    public File getRootFile() {
        return sourceRootPattern.rootFile();
    }

    @Override
    public Sx.BuildTarget<?> getTarget() {
        return target;
    }

    @Override
    @NotNull
    public SxForm sxForm() {
        var properties = SxFormUtils.tagged()
                .with("targetId", name(target.getId()))
                .with("targetPresentableName", name(target.getPresentableName()))
                .with("sourceRootPattern", sourceRootPattern.sxForm());
        return list().add(name("SourceRoot")).add(properties);
    }
}
