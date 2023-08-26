package aldor.builder.jps.autoconf;

import aldor.builder.jps.util.Sx;
import aldor.util.SxForm;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;

public class NoOpTargetBuilder<R extends Sx.BuildRootDescriptor> extends Sx.TargetBuilder<R, Sx.BuildTarget<R>> {
    private static final Logger LOG = Logger.getInstance(NoOpTargetBuilder.class);
    private final List<? extends Sx.BuildTargetType<? extends Sx.BuildTarget<R>>> types;
    private final Statistics statistics = new Statistics();

    public NoOpTargetBuilder(List<? extends Sx.BuildTargetType<? extends Sx.BuildTarget<R>>> types) {
        super(types);
        this.types = types;
    }

    @Override
    public @NotNull SxForm sxForm() {
        var l = list().add(name("NoOpTargetBuilder"));
        for (Sx.BuildTargetType<? extends Sx.BuildTarget<R>> t : types) {
            l = l.add(t.sxForm());
        }
        return l;
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getPresentableName() {
        return "Builder for " + this.getTargetTypes().stream().map(BuildTargetType::getTypeId).collect(Collectors.joining(", "));
    }

    @Override
    public void buildStarted(CompileContext context) {
        super.buildStarted(context);
        statistics.startRound();
    }

    @Override
    public void buildFinished(CompileContext context) {
        super.buildFinished(context);
        statistics.log();
        statistics.endRound();
    }

    @Override
    public void build(Sx.@NotNull BuildTarget<R> target, @NotNull DirtyFilesHolder<R, Sx.BuildTarget<R>> holder,
                      @NotNull BuildOutputConsumer outputConsumer, @NotNull CompileContext context) throws ProjectBuildException, IOException {
        LOG.debug("Build " + target.getPresentableName());
        context.processMessage(new CompilerMessage("PhonyBuilder", BuildMessage.Kind.INFO, target.getPresentableName()));
        if (!holder.hasDirtyFiles()) {
            statistics.cleanTarget();
        }
        statistics.completeBuild();
    }

    private static class Statistics {
        private int roundsStarted = 0;
        private int roundsFinished = 0;
        private int targetsBuiltThisRound = 0;
        private int cleanTargetsThisRound = 0;

        void startRound() {
            roundsStarted++;
            targetsBuiltThisRound = 0;
            cleanTargetsThisRound = 0;
        }

        void completeBuild() {
            targetsBuiltThisRound++;
        }

        void endRound() {
            roundsFinished++;
        }

        void log() {
            LOG.info(String.format("Rounds started: %d, rounds finished: %d. This round - built: %d clean: %d", roundsStarted, roundsFinished, targetsBuiltThisRound, cleanTargetsThisRound));
        }

        public void cleanTarget() {
            cleanTargetsThisRound++;
        }
    }

}
