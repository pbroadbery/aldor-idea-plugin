package aldor.module.template.detect;

import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

class ConfigBuildRootCandidate extends DetectedProjectRoot {
    private static final Logger LOG = Logger.getInstance(ConfigBuildRootCandidate.class);

    protected ConfigBuildRootCandidate(@NotNull File directory) {
        super(directory);
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getRootTypeName() {
        return "AldorBuildRoot";
    }

    @Override
    public boolean canContainRoot(@NotNull DetectedProjectRoot root) {
        LOG.info("Build root can contain " + root.getRootTypeName() + " " + root.getDirectory());
        return false; // Maybe should allow some java builds?
    }

    @Override
    public String toString() {
        return "BR[" + getDirectory() + "]";
    }
}
