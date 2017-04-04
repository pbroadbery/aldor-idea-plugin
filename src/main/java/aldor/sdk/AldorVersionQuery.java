package aldor.sdk;

import aldor.util.AnnotatedOptional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static aldor.util.AnnotatedOptional.missing;
import static aldor.util.AnnotatedOptional.of;

public class AldorVersionQuery {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Pattern VERSION_PATTERN = Pattern.compile("Aldor version ", Pattern.LITERAL);
    private static final Pattern FRICAS_VERSION_PATTERN = Pattern.compile("Version: ", Pattern.LITERAL);

    boolean isAldorBaseDirectory(String path) {
        File file = new File(path);
        if (!file.isDirectory()) {
            return false;
        }
        File binary = new File(new File(path), "bin");
        binary = new File(binary, "aldor");
        if (binary.canExecute()) {
            return false;
        }
        return true;
    }

    public AnnotatedOptional<String,String> aldorVersion(String path) {
        return readProcessOutput(Arrays.asList(path, "-v")).flatMap(this::aldorVersionFromOutput);
    }


    public AnnotatedOptional<String,String> fricasVersion(String path) {
        return readProcessOutput(Arrays.asList(path, "-nosman")).flatMap(this::fricasVersionFromOutput);
    }


    public AnnotatedOptional<String,String> aldorVersionFromOutput(List<String> l) {
        if (l.isEmpty()) {
            return missing("No output from aldor commmand");
        }
        else {
            String versionLine = l.get(0);
            return of(VERSION_PATTERN.matcher(versionLine).replaceAll(Matcher.quoteReplacement("")));
        }
    }


    public AnnotatedOptional<String,String> fricasVersionFromOutput(Collection<String> l) {
        if (l.size() < 2) {
            return missing("No output from aldor commmand");
        }
        else {
            return AnnotatedOptional.fromOptional(l.stream()
                                                   .filter(line -> line.contains("Version:"))
                                                   .map(line -> FRICAS_VERSION_PATTERN.matcher(line)
                                                                    .replaceAll(Matcher.quoteReplacement("")).trim())
                                                   .findFirst(), () -> "Missing version line");

        }
    }


    public AnnotatedOptional<List<String>, String> readProcessOutput(List<String> command) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        Process process = null;
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
