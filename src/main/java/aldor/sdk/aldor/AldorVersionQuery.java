package aldor.sdk.aldor;

import aldor.sdk.IProbeRunner;
import aldor.sdk.OsDetails;
import aldor.sdk.ProbeRunner;
import aldor.util.AnnotatedOptional;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static aldor.util.AnnotatedOptional.missing;
import static aldor.util.AnnotatedOptional.of;

public class AldorVersionQuery {
    private static final Pattern VERSION_PATTERN = Pattern.compile("Aldor version ", Pattern.LITERAL);
    private static final Pattern FRICAS_VERSION_PATTERN = Pattern.compile("Version: ", Pattern.LITERAL);

    private final OsDetails osDetails;
    private final IProbeRunner probe;

    public AldorVersionQuery() {
        this(new OsDetails(), new ProbeRunner());
    }

    public AldorVersionQuery(OsDetails osDetails, IProbeRunner probe) {
        this.osDetails = osDetails;
        this.probe = probe;
    }

    boolean isAldorBaseDirectory(String path) {
        File file = new File(path);
        if (!file.isDirectory()) {
            return false;
        }
        File binary = new File(new File(path), "bin");
        binary = new File(binary, "aldor");
        if (!binary.canExecute()) {
            return false;
        }
        return true;
    }

    public AnnotatedOptional<String,String> aldorVersion(String path) {
        return probe.readProcessOutput(Arrays.asList(path, "-v"), Collections.emptyMap()).flatMap(this::aldorVersionFromOutput);
    }


    public AnnotatedOptional<String,String> fricasVersion(String root) {
        return fricasExecution(root).flatMap(this::fricasVersionFromOutput);
    }

    public AnnotatedOptional<String,String> aldorVersionFromOutput(List<String> l) {
        if (l.isEmpty()) {
            return missing("No output from aldor command");
        }
        else {
            String versionLine = l.get(0);
            return of(VERSION_PATTERN.matcher(versionLine).replaceAll(Matcher.quoteReplacement("")));
        }
    }

    AnnotatedOptional<List<String>, String> fricasExecution(String root) {
        if (osDetails.isWindows()) {
            return probe.readProcessOutput(Collections.singletonList(root + "/bin/FRICASsys"), Collections.singletonMap("FRICAS", root));
        }
        else {
            return probe.readProcessOutput(List.of(root + "/bin/fricas", "-nosman"), Collections.emptyMap());
        }
    }

    public String fricasPath(String homePath) {
        if (osDetails.isWindows()) {
            return homePath + "/bin/FRICASsys";
        }
        else {
            return homePath + "/bin/fricas";
        }
    }

    public AnnotatedOptional<String,String> fricasVersionFromOutput(Collection<String> l) {
        if (l.size() < 2) {
            return missing("No output from fricas command");
        }
        else {
            return AnnotatedOptional.fromOptional(l.stream()
                                                   .filter(line -> line.contains("Version:"))
                                                   .map(line -> FRICAS_VERSION_PATTERN.matcher(line)
                                                                    .replaceAll(Matcher.quoteReplacement("")).trim())
                                                   .findFirst(), () -> "Missing version in output");

        }
    }

}
