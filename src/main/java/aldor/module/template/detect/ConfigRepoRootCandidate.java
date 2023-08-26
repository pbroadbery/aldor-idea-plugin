package aldor.module.template.detect;

import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

class ConfigRepoRootCandidate extends DetectedProjectRoot {
    private static final Logger LOG = Logger.getInstance(ConfigRepoRootCandidate.class);

    protected ConfigRepoRootCandidate(@NotNull File directory) {
        super(directory);
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getRootTypeName() {
        return "ConfigRepoRoot";
    }

    @Override
    public boolean canContainRoot(@NotNull DetectedProjectRoot root) {
        LOG.info("Can contain " + root.getRootTypeName() + " " + root.getDirectory());
        return !"Java".equals(root.getRootTypeName());
    }

    @Override
    public String toString() {
        return "CR[" + getDirectory() + "]";
    }
}
