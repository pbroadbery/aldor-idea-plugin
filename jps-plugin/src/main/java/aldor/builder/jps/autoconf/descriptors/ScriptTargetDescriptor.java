package aldor.builder.jps.autoconf.descriptors;

import aldor.util.HasSxForm;
import aldor.util.SxForm;
import aldor.util.SxFormUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static aldor.util.SxFormUtils.file;

// We'll have multiple TargetTypes which will all use the same ScriptType layout... so enough in here to
// create a target.
// Basically, a scriptType & other stuff
public class ScriptTargetDescriptor implements AbstractTargetDescriptor, HasSxForm {
    @Nonnull
    private final ScriptType scriptType;
    @Nonnull
    private final List<RootDescriptor> rootDescriptors;
    @Nonnull
    private final List<File> outputRoots;
    @Nonnull
    private final File srcTopDir;
    @Nonnull
    private final File buildTopDir;

    public ScriptTargetDescriptor(@Nonnull ScriptType scriptType,
                                  @Nonnull File srcTopDir,
                                  @Nonnull File buildTopDir) {
        this.scriptType = scriptType;
        this.srcTopDir = srcTopDir;
        this.buildTopDir = buildTopDir;
        this.rootDescriptors = new ArrayList<>();
        this.outputRoots = new ArrayList<>();
    }

    public void addRootDescriptor(RootDescriptor rootDescriptor) {
        rootDescriptors.add(rootDescriptor);
    }

    @Override
    public @NotNull SxForm sxForm() {
        return SxFormUtils.list()
                .add(SxFormUtils.name("ScriptTgtDescriptor"))
                .add(SxFormUtils.tagged()
                        .with("id", this::id)
                        .with("srcTopDir", file(srcTopDir))
                        .with("buildTopDir", file(buildTopDir))
                        .with("scriptType", scriptType.sxForm()));
    }

    @Override
    public String id() {
        return scriptType.kind().name() + "-" + scriptType.targetName() + "-" + scriptType.subdirectory();
    }

    public void addOutputRoot(File rootDirectory) {
        outputRoots.add(rootDirectory);
    }

    public ScriptType scriptType() {
        return scriptType;
    }

    public String subdirectory() {
        return scriptType.subdirectory();
    }

    public Collection<File> outputRoots() {
        return outputRoots;
    }

    public File topSourceDirectory() {
        // TODO: Decide on nicest name
        return srcTopDir;
    }

    public File topBuildDirectory() {
        // TODO: Decide on nicest name
        return buildTopDir;
    }

    public Collection<RootDescriptor> rootDescriptors() {
        return rootDescriptors;
    }

    public static class RootDescriptor implements HasSxForm {
        private final String id;
        private final SourceRootPattern pattern;

        public RootDescriptor(String id, SourceRootPattern pattern) {
            this.id = id;
            this.pattern = pattern;
        }

        public SourceRootPattern pattern() {
            return pattern;
        }

        @Override
        public @NotNull SxForm sxForm() {
            return SxFormUtils.list()
                    .add(SxFormUtils.name("RootDescriptor"))
                    .add(SxFormUtils.tagged()
                            .with("id", SxFormUtils.name(id))
                            .with("pattern", pattern.sxForm()));
        }
    }
}
