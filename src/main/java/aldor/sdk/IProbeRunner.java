package aldor.sdk;

import aldor.util.AnnotatedOptional;

import java.util.List;
import java.util.Map;

public interface IProbeRunner {
    public AnnotatedOptional<List<String>, String> readProcessOutput(List<String> command, Map<String, String> environment);
}
