package aldor.sdk;

import aldor.util.AnnotatedOptional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static aldor.util.AnnotatedOptional.missing;
import static aldor.util.AnnotatedOptional.of;

public class ProbeRunner implements IProbeRunner {
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public AnnotatedOptional<List<String>, String> readProcessOutput(List<String> command, Map<String, String> environment) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        builder.environment().putAll(environment);
        Process process;
        try {
            process = builder.start();
            process.getOutputStream().close();
        } catch (IOException e) {
            return missing("Failed to start process: " + e.getMessage());
        }
        InputStream stream = process.getInputStream();
        Future<AnnotatedOptional<List<String>, String>> l = executor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))) {
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                return of(lines);
            } catch (IOException e) {
                return missing("IO Exception: " + e.getMessage());
            }
        });
        try {
            return l.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return missing("Interrupted! " + e.getMessage());
        } catch (ExecutionException e) {
            return missing("Execution failed: " + e.getMessage());
        } catch (TimeoutException ignored) {
            return missing("Timed out");
        }
    }
}
