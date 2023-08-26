package aldor.make;

import aldor.builder.files.CompileRunner;
import aldor.util.InstanceCounter;
import com.google.common.base.Charsets;
import com.google.common.collect.Streams;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple compiler for aldor - just call make.
 */
public class FullCompileRunner implements CompileRunner {
    @SuppressWarnings("WeakerAccess")
    public static final String ALDOR_COMPILER = "aldor compiler";
    private static final Logger LOG = Logger.getInstance(FullCompileRunner.class);
    private final int instanceId = InstanceCounter.instance().next(FullCompileRunner.class);
    private final CompileContext context;
    private final ExecutorService executor;

    public FullCompileRunner(ExecutorService executor, CompileContext context) {
        this.executor = executor;
        this.context = context;
    }

    @Override
    public String id() {
        return "{Compiler-" + instanceId +"}";
    }

    @Override
    public boolean compileOneFile(@NotNull File buildDirectory, @NotNull String targetName, Set<MakeOption> options) {
        try {
            if (!buildDirectory.getCanonicalFile().exists()) {
                context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "Build directory does not exist: " + buildDirectory));
            }
            GenericOutputParser errorParser = new CompileOutputParser(ALDOR_COMPILER, buildDirectory, context::processMessage);
            Set<MakeOption> fullOptions = fillOptions(options);
            ProcessBuilder processBuilder = buildMakeProcess(buildDirectory, targetName, fullOptions);
            return doBuild(processBuilder, errorParser, new CommandOutputParser("make", context::processMessage), buildDirectory, targetName);
        } catch (IOException e) {
            context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "IO Error on build: " + e.getMessage()));
            return false;
        }

    }

    @NotNull
    private static Set<MakeOption> fillOptions(Collection<MakeOption> options) {
        Set<MakeOption> fullOptions = new HashSet<>();
        //fullOptions.add(MakeOption.Trace);
        fullOptions.addAll(options);
        return fullOptions;
    }

    @Override
    public boolean runAutogen(@NotNull File sourceDirectory, @NotNull String targetName) {
        try {
            if (!sourceDirectory.getAbsoluteFile().isDirectory()) {
                context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "Source directory does not exist: " + sourceDirectory));
                return false;
            }
            else {
                ProcessBuilder processBuilder = buildAutogenProcess(sourceDirectory.getCanonicalFile());
                return doBuild(processBuilder,
                            new CommandOutputParser("autogen.sh", context::processMessage),
                            new CommandOutputParser("autogen.sh (stderr)", context::processMessage),
                            sourceDirectory.getCanonicalFile(), targetName);
            }
        } catch (IOException e) {
            context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "IO Error on build: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public boolean runConfigure(@NotNull File buildDirectory, File sourceDirectory, String targetName, List<String> args) {
        try {
            if (!sourceDirectory.getCanonicalFile().isDirectory()) {
                context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "Source directory does not exist: " + sourceDirectory));
                return false;
            }
            if (!new File(sourceDirectory.getAbsolutePath(), "configure").canExecute()) {
                context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "No configuration script in : " + sourceDirectory));
                return false;
            }
            if (!buildDirectory.getCanonicalFile().isDirectory()) {
                context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.INFO, "Build directory does not exist - creating: " + buildDirectory));
                boolean created = buildDirectory.mkdirs();
                if (!created) {
                    context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "Failed to create build directory: " + buildDirectory));
                    LOG.info("Failed to create target directory: "+ buildDirectory.getCanonicalFile());
                    return false;
                }
            }

            ProcessBuilder processBuilder = buildConfigureProcess(sourceDirectory.getCanonicalFile(), buildDirectory.getCanonicalFile(), args);
            return doBuild(processBuilder,
                    new CommandOutputParser("configure", context::processMessage),
                    new CommandOutputParser("configure (stderr)", context::processMessage),
                    buildDirectory.getCanonicalFile(), targetName);
        } catch (IOException e) {
            context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "IO Error on build: " + e.getMessage()));
            return false;
        }
    }

    private ProcessBuilder buildAutogenProcess(File sourceDirectory) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("./autogen.sh");
        processBuilder.directory(sourceDirectory);
        return processBuilder;
    }


    @SuppressWarnings("TypeMayBeWeakened")
    private ProcessBuilder buildConfigureProcess(File sourceDirectory, File targetDirectory, List<String> args) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> allArgs = Streams.concat(Stream.of(sourceDirectory.getAbsolutePath() + "/configure"), args.stream()).toList();
        processBuilder.command(allArgs.toArray(n -> new String[n]));
        processBuilder.directory(targetDirectory);
        return processBuilder;
    }

    @NotNull
    private static ProcessBuilder buildMakeProcess(File path, String target, @SuppressWarnings("TypeMayBeWeakened") Set<MakeOption> options) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(Streams.concat(Stream.of("make"), options.stream().flatMap(x -> x.options().stream()), Stream.of(target)).collect(Collectors.toList()));
        processBuilder.directory(path);
        return processBuilder;
    }

    private boolean doBuild(ProcessBuilder processBuilder, GenericOutputParser stdoutParser, GenericOutputParser stderrParser, final File buildDirectory,
                         final String target) throws IOException {

        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        LOG.info("Starting build: " + processBuilder.command() + " @ " + processBuilder.directory());

        context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.INFO, "Running process: " + processBuilder.command()));
        context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.INFO, "Running process in: " + processBuilder.directory()));
        Process process = processBuilder.start();
        // FIXME: Use netty here (see ExternalJavacManager in intellij)
        Future<?> stdErrFut = executor.submit(() -> watchStdError(process, stderrParser));
        Future<?> stdOutFut = executor.submit(() -> watchStdOut(target, process, stdoutParser));
        int status = -1;
        try {
            status = process.waitFor();
            stdErrFut.get();
            stdOutFut.get();
        } catch (InterruptedException ignored) {
            //noinspection ResultOfMethodCallIgnored
            Thread.interrupted();
            context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.WARNING, "Interrupted! "));
        } catch (ExecutionException e) {
            LOG.error("Message read threw an error: ", e);
            context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "Failed to read output: " + e.getMessage() + " " + e.getCause()));
        }
        LOG.info("Build complete(" + status + "): " + processBuilder.command() + " @ " + processBuilder.directory());
        return status == 0;
    }

    private void watchStdError(Process process,  GenericOutputParser errorParser) {
        Reader reader = new InputStreamReader(process.getErrorStream(), Charsets.US_ASCII);
        try (BufferedReader lineReader = new BufferedReader(reader)) {
            String line;
            while ((line = lineReader.readLine()) != null) {
                errorParser.newMessage(line);
                LOG.info("From compiler (stderr): " + line);
            }
        } catch (IOException e) {
            LOG.info("Error while reading stderr: " + e.getMessage());
        }
    }

    private void watchStdOut(String target, Process process, GenericOutputParser errorParser) {
        Reader reader = new InputStreamReader(process.getInputStream(), Charsets.US_ASCII);
        try (BufferedReader lineReader = new BufferedReader(reader)) {
            String line;
            while ((line = lineReader.readLine()) != null) {
                errorParser.newMessage(line);
                LOG.info("From compiler (stdout): " + line);
            }
        } catch (IOException e) {
            LOG.info("Error while reading stdout: " + e.getMessage());
        }
        finally {
            errorParser.close();
            LOG.info("Reached end of file " + target);
        }
    }
}
