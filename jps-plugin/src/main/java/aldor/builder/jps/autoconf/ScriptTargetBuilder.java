package aldor.builder.jps.autoconf;

import aldor.builder.files.CompileRunner;
import aldor.builder.jps.autoconf.descriptors.ScriptTargetDescriptor;
import aldor.builder.jps.autoconf.descriptors.ScriptType;
import aldor.builder.jps.util.Sx;
import aldor.make.FullCompileRunner;
import aldor.make.MakeOption;
import aldor.util.HasSxForm;
import aldor.util.SxForm;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.FileProcessor;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static aldor.util.SxFormUtils.SxListForm;
import static aldor.util.SxFormUtils.collectList;
import static aldor.util.SxFormUtils.file;
import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;

public class ScriptTargetBuilder extends Sx.TargetBuilder<SimpleSourceRoot, ScriptBuildTarget> {
    private static final Logger LOG = Logger.getInstance(ScriptTargetBuilder.class);
    private final StaticExecutionEnvironment executionEnvironment;
    private Set<BuildTarget<?>> toBuild = null;
    private final Statistics statistics = new Statistics();

    public ScriptTargetBuilder(StaticExecutionEnvironment executionEnvironment,
                               ScriptBuildTargetType buildTargetType) {
        super(List.of(buildTargetType));
        this.executionEnvironment = executionEnvironment;
    }


    @Override
    public void buildStarted(CompileContext context) {
        // TODO: Maybe initialise some stats gathering here
        LOG.info("Build started");

        var all = context.getProjectDescriptor().getBuildTargetIndex().getAllTargets(PhonyTargets.ident.findType());
        var scripts = all.stream().filter(tgt -> context.getScope().isAffected(tgt)).toList();
        this.toBuild = TargetFactory.transitiveDependencies(context.getProjectDescriptor().getBuildTargetIndex(), scripts);
        LOG.info("Found " + toBuild.size() + " targets to build");
        statistics.startRound(toBuild.size());
    }

    @Override
    public void buildFinished(CompileContext context) {
        statistics.endRound();
        statistics.log();
        LOG.info("Build finished");
        this.toBuild = Collections.emptySet();
    }


    @Override
    public void build(@NotNull ScriptBuildTarget target,
                      @NotNull DirtyFilesHolder<SimpleSourceRoot, ScriptBuildTarget> holder,
                      @NotNull BuildOutputConsumer outputConsumer,
                      @NotNull CompileContext context) throws ProjectBuildException, IOException {

        if ((toBuild != null) && !toBuild.contains(target)) {
            LOG.debug("Skip: " + target.getPresentableName());
            statistics.skipped();
            return;
        }
        statistics.startBuild();
        if (!holder.hasDirtyFiles()) {
            LOG.info("Clean: " + target.getPresentableName());
            statistics.cleanTarget();
            return;
        }
        context.getProjectDescriptor().getBuildTargetIndex().getDependencies(target, context);
        context.getProjectDescriptor().getTargetsState().getBuildTargetId(target);
        logCompileStart(target, holder, context);
        LoggedOutputConsumer loggedConsumer = logged(outputConsumer);
        boolean completed = false;
        try {
            switch (target.scriptType().kind()) {
                case Autogen -> {
                    runAutogen(target.scriptDescriptor(), holder, loggedConsumer, context);
                }
                case Configure -> {
                    runConfigure(target.scriptDescriptor(), holder, loggedConsumer, context);
                }
                case Make -> {
                    runMake(target.scriptDescriptor(), holder, loggedConsumer, context);
                }
            }
            completed = true;
            statistics.completeBuild();
        }
        finally {
            logCompileFinish(target, context, loggedConsumer, completed);
        }
    }

    @Override
    @NotNull @Nls(capitalization = Nls.Capitalization.Sentence)
    public String getPresentableName() {
        return "Script Builder";
    }

    private void runAutogen(ScriptTargetDescriptor descriptor,
                            @NotNull DirtyFilesHolder<SimpleSourceRoot, ScriptBuildTarget> holder,
                            @NotNull BuildOutputConsumer outputConsumer,
                            @NotNull CompileContext context) throws IOException, ProjectBuildException {
        CompileRunner compiler = CompileRunner.logged(new FullCompileRunner(executionEnvironment.executorService(), context)); // Factory..
        File root = descriptor.topSourceDirectory();
        compiler.runAutogen(root, "autogen");
        context.setDone(CompilerConstants.mostlyDone);
        var m4Files = new File(root, "m4").list();
        List<File> files = new ArrayList<>();
        File file = new File(root, "configure.ac");
        if (!file.exists()) {
            context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.ERROR, "Failed to create configure script"));
            throw new ProjectBuildException("Failed to create configure script");
        }
        files.add(file);
        if (m4Files != null) {
            Arrays.stream(m4Files)
                    .map(x -> new File(root, x))
                    .forEach(f -> files.add(f));
        }
        List<String> niceFiles = files.stream().map(f -> FileUtil.toSystemIndependentName(f.getAbsolutePath())).toList();
        outputConsumer.registerOutputFile(new File(root, "configure"), niceFiles);
        context.setDone(CompilerConstants.allDone);
    }

    private void runConfigure(ScriptTargetDescriptor scriptTargetDescriptor,
                              DirtyFilesHolder<SimpleSourceRoot, ScriptBuildTarget> holder,
                              BuildOutputConsumer outputConsumer,
                              CompileContext context) throws IOException {
        CompileRunner compiler = CompileRunner.logged(new FullCompileRunner(executionEnvironment.executorService(), context)); // Factory..
        File root = scriptTargetDescriptor.topSourceDirectory();
        File target = scriptTargetDescriptor.topBuildDirectory();
        var args = Optional.ofNullable(scriptTargetDescriptor.scriptType().options(ScriptType.ScriptOptions.Configure)).orElse(List.of());
        compiler.runConfigure(target, root, "configure", args);
        context.setDone(CompilerConstants.mostlyDone);
        //outputConsumer.registerOutputDirectory(target, List.of(new File(root, "configure").getAbsolutePath()));
        outputConsumer.registerOutputFile(new File(root, "configure"), List.of(new File(scriptTargetDescriptor.topSourceDirectory(), "configure.am").getPath()));
        context.setDone(CompilerConstants.allDone);
    }

    private void runMake(ScriptTargetDescriptor scriptTargetDescriptor,
                            @NotNull DirtyFilesHolder<SimpleSourceRoot, ScriptBuildTarget> holder,
                            @NotNull BuildOutputConsumer outputConsumer,
                            @NotNull CompileContext context) throws IOException {
        CompileRunner compiler = CompileRunner.logged(new FullCompileRunner(executionEnvironment.executorService(), context)); // Factory..
        compiler.compileOneFile(new File(scriptTargetDescriptor.topBuildDirectory(),
                                scriptTargetDescriptor.subdirectory()),
                                scriptTargetDescriptor.scriptType().targetName(), Set.of(MakeOption.Parallel));
        context.setDone(CompilerConstants.mostlyDone);
        //outputConsumer.registerOutputDirectory(targetDirectory, List.of(new File(root, "configure").getAbsolutePath()));
        context.setDone(CompilerConstants.allDone);
    }


    @Override
    public @NotNull SxForm sxForm() {
        return list().add(name("ScriptTargetBuilder"));
    }
    private LoggedOutputConsumer logged(BuildOutputConsumer outputConsumer) {
        return new LoggedOutputConsumer(outputConsumer);
    }

    void logCompileStart(ScriptBuildTarget target,  DirtyFilesHolder<SimpleSourceRoot, ScriptBuildTarget> holder, CompileContext context) {
        LogToContext logger = new LogToContext(target.getId(), LOG, context);
        logger.info("Starting compilation for " + target.getPresentableName());
        logger.info("Target: " + target.sxForm().asSExpression());
        try {
            logger.info("Holder - dirtyFiles: " + holder.hasDirtyFiles());
            logger.info("Holder - removed (this tgt): " + holder.getRemovedFiles(target));
            holder.processDirtyFiles(new FileProcessor<>() {
                @Override
                public boolean apply(ScriptBuildTarget dirtyTarget, File file, SimpleSourceRoot root) throws IOException {
                    logger.info("Holder - dirty: " + dirtyTarget.getId() + " " + file + " " + root.getRootId());
                    return true;
                }
            });
        } catch (IOException e) {
            LOG.error("oops");
            LOG.error(e);
        }
    }

    private void logCompileFinish(ScriptBuildTarget target, CompileContext context, LoggedOutputConsumer loggedConsumer, boolean completed) {
        LogToContext logger = new LogToContext(target.getId(), LOG, context);
        logger.info("Finished compile for [" + target.getPresentableName() + "] Status: " + completed);
    }

    // ToDo: Use elsewhere, and control via an option somewhere...
    private static class LogToContext {
        private final Logger logger;
        private final CompileContext context;
        private final String targetName;

        LogToContext(String targetName, Logger logger, CompileContext context) {
            this.targetName = targetName;
            this.logger = logger;
            this.context = context;
        }

        void info(String message) {
            logger.info(message);
            context.processMessage(new CompilerMessage(targetName, BuildMessage.Kind.INFO, message));
        }
    }

    private static class LoggedOutputConsumer implements BuildOutputConsumer, HasSxForm {
        private final List<Trinity<String, File, Collection<String>>> files = new ArrayList<>();
        private final BuildOutputConsumer outputConsumer;

        public LoggedOutputConsumer(BuildOutputConsumer outputConsumer) {
            this.outputConsumer = outputConsumer;
        }

        @Override
        public void registerOutputFile(@NotNull File outputFile, @NotNull Collection<String> sourcePaths) throws IOException {
            files.add(new Trinity<>("file", outputFile, sourcePaths));
            outputConsumer.registerOutputFile(outputFile, sourcePaths);
        }

        @Override
        public void registerOutputDirectory(@NotNull File outputDir, @NotNull Collection<String> sourcePaths) throws IOException {
            files.add(new Trinity<>("dir", outputDir, sourcePaths));
            outputConsumer.registerOutputDirectory(outputDir, sourcePaths);
        }

        @Override
        public @NotNull SxForm sxForm() {
            SxListForm list = list();
            for (Trinity<String, File, Collection<String>> t : files) {
                var tl = t.third.stream().map(s -> name(s)).collect(collectList());
                list.add(list().add(name(t.first)).add(file(t.second)).add(tl));
            }
            return list;
        }
    }

    static final class CompilerConstants {
        public static final float mostlyDone = 0.95f;
        public static final float allDone = 0.999f;
    }

    private static class Statistics {
        private int roundsStarted = 0;
        private int roundsFinished = 0;
        private int targetsStartedThisRound = 0;
        private int targetsBuiltThisRound = 0;
        private int targetsToBuildThisRound = 0;
        private int cleanTargetsThisRound = 0;
        private int skippedTargetsThisRound = 0;

        void startRound(int count) {
            roundsStarted++;
            targetsToBuildThisRound = count;
            targetsBuiltThisRound = 0;
            targetsStartedThisRound = 0;
            cleanTargetsThisRound = 0;
        }

        void startBuild() {
            targetsStartedThisRound++;
        }
        void completeBuild() {
            targetsBuiltThisRound++;
        }

        void endRound() {
            roundsFinished++;
        }

        void log() {
            LOG.info(String.format("Rounds started: %d, rounds finished: %d. This round: targetsToBuild: %d built: %d clean: %d", roundsStarted, roundsFinished, targetsToBuildThisRound, targetsBuiltThisRound, cleanTargetsThisRound));
        }

        public void cleanTarget() {
            cleanTargetsThisRound++;
        }

        public void skipped() {
            skippedTargetsThisRound++;
        }
    }
}
