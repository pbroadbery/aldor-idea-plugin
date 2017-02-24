package aldor.module.template;

import com.google.common.base.Charsets;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GitProcess {
    private static final Logger LOG = Logger.getInstance(GitProcess.class);
    private final ExecutorService executorService;

    public GitProcess() {
        executorService = Executors.newSingleThreadExecutor();
    }

    void runCommand(File path, List<String> args) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(args.toArray(new String[args.size()]));
        processBuilder.directory(path);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        LOG.info("Starting build: " + processBuilder.command() + " @ " + processBuilder.directory());

        Process process = processBuilder.start();

        // FIXME: Use netty here (see ExternalJavacManager in intellij)
        Future<?> stdErrFut = executorService.submit(() -> watchStdError(process));
        Future<?> stdOutFut = executorService.submit(() -> watchStdOut(path, process));

        try {
            process.waitFor();
            stdErrFut.get();
            stdOutFut.get();
        } catch (InterruptedException ignored) {
            Thread.interrupted();
        } catch (ExecutionException e) {
            LOG.error("Message read threw an error: ", e);
        }
        LOG.info("Build complete: " + processBuilder.command() + " @ " + processBuilder.directory());
    }

    private void watchStdError(Process process) {
        Reader reader = new InputStreamReader(process.getErrorStream(), Charsets.US_ASCII);
        try (BufferedReader lineReader = new BufferedReader(reader)) {
            String line;
            while ((line = lineReader.readLine()) != null) {
                //context.processMessage(new CompilerMessage(ALDOR_COMPILER, BuildMessage.Kind.ERROR, line));
                LOG.info("Error from compiler: " + line);
            }
        } catch (IOException e) {
            LOG.info("Error while reading stderr: " + e.getMessage());
        }
    }

    private void watchStdOut(File baseDirectory, Process process) {
        Reader reader = new InputStreamReader(process.getInputStream(), Charsets.US_ASCII);
        try (BufferedReader lineReader = new BufferedReader(reader)) {
            String line;
            while ((line = lineReader.readLine()) != null) {
                LOG.info("Info from compiler: " + line);
            }
        } catch (IOException e) {
            LOG.info("Error while reading stdout: " + e.getMessage());
        }
        finally {
            LOG.info("Reached end of file ");
        }
    }


}
