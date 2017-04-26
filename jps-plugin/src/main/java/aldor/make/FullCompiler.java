package aldor.make;

import aldor.builder.files.AldorFileBuildTarget;
import aldor.builder.files.AldorFileBuildTargetType;
import aldor.builder.files.AldorFileRootDescriptor;
import aldor.builder.files.AldorFileTargetBuilder;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.intellij.util.io.IOUtil.US_ASCII;

/**
 * Simple compiler for aldor - just call make.
 */
public class FullCompiler implements AldorFileTargetBuilder.Compiler {
    private static final Logger LOG = Logger.getInstance(FullCompiler.class);
    public static final String ALDOR_COMPILER = "aldor compiler";
    private final CompileContext context;

    public FullCompiler(DirtyFilesHolder<AldorFileRootDescriptor, AldorFileBuildTarget> holder, BuildOutputConsumer outputConsumer, CompileContext context) {
        this.context = context;
    }

    @Override
    public boolean compileOneFile(AldorFileBuildTarget target, File file, AldorFileRootDescriptor descriptor) {
        ExecutorService service = AldorFileBuildTargetType.executorFor(descriptor);
        File buildDirectory = descriptor.buildDirectoryForFile(file);
        if (buildDirectory == null) {
            context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "Missing configuration for " + file));
            return false;
        }
        if (!buildDirectory.exists()) {
            context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "Build directory does not exist: " + buildDirectory));
        }
        try {
            doBuild(service, buildDirectory, target.targetForFile(file.getName()));
        } catch (IOException e) {
            context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "IO Error on build: " + e.getMessage()));
            return false;
        }

        return true;
    }

    void doBuild(ExecutorService executorService, final File path, final String target) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("make", target);
        processBuilder.directory(path);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        LOG.info("Starting build: " + processBuilder.command() + " @ " + processBuilder.directory());

        context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.INFO, "Running process: " + processBuilder.command()));
        Process process = processBuilder.start();

        // FIXME: Use netty here (see ExternalJavacManager in intellij)
        Future<?> stdErrFut = executorService.submit(() -> watchStdError(target, process));
        Future<?> stdOutFut = executorService.submit(() -> watchStdOut(target, path, process));

        try {
            process.waitFor();
            stdErrFut.get();
            stdOutFut.get();
        } catch (InterruptedException ignored) {
            Thread.interrupted();
        } catch (ExecutionException e) {
            LOG.error("Message read threw an error: ", e);
            context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, "Failed to read output: " + e.getMessage() + " " + e.getCause()));
        }
        LOG.info("Build complete: " + processBuilder.command() + " @ " + processBuilder.directory());
    }

    private void watchStdError(String target, Process process) {
        Reader reader = new InputStreamReader(process.getErrorStream(), US_ASCII);
        try (BufferedReader lineReader = new BufferedReader(reader)) {
            String line;
            while ((line = lineReader.readLine()) != null) {
                context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, line));
                LOG.info("Error from compiler: " + line);
            }
        } catch (IOException e) {
            LOG.info("Error while reading stderr: " + e.getMessage());
        }
    }

    private void watchStdOut(String target, File baseDirectory, Process process) {
        CompileOutputParser errorParser = new CompileOutputParser(ALDOR_COMPILER, baseDirectory, context::processMessage);
        Reader reader = new InputStreamReader(process.getInputStream(), US_ASCII);
        try (BufferedReader lineReader = new BufferedReader(reader)) {
            String line;
            while ((line = lineReader.readLine()) != null) {
                errorParser.newMessage(line);
                //context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.INFO, line));
                LOG.info("Info from compiler: " + line);
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
