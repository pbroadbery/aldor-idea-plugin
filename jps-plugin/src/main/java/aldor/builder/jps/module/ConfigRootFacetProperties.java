package aldor.builder.jps.module;

import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConfigRootFacetProperties {
    @Nullable
    @Attribute("buildDirectory")
    private final String buildDirectory;
    @Nullable
    @Attribute("installDirectory")
    private final String installDirectory;
    @Attribute("defined")
    private final boolean defined;
    /*
    @Nullable
    @Attribute("options")
    private final String options;
    */
    // TODO: List of configure arguments

    public ConfigRootFacetProperties(boolean defined, String buildDirectory, String installDirectory) {
        this.defined = defined;
        this.buildDirectory = buildDirectory;
        this.installDirectory = installDirectory;
    }

    public ConfigRootFacetProperties() {
        this(false, null, null);
    }

    @NotNull
    public ConfigRootFacetProperties.Builder asBuilder() {
        return newBuilder()
                .setBuildDirectory(this.buildDirectory)
                .setInstallDirectory(this.installDirectory)
                .setDefined(defined);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String buildDirectory() {
        return buildDirectory;
    }

    public String installDirectory() {
        return installDirectory;
    }

    public List<String> configureArguments() {
        return Collections.emptyList();
    }

    public ConfigRootFacetProperties copy() {
        return this.asBuilder().build();
    }
    @Override
    public String toString() {
        return "ConfiguredRootFacetProperties{" +
                "defined=" + defined +
                ", buildDirectory=" + buildDirectory + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        ConfigRootFacetProperties that = (ConfigRootFacetProperties) o;
        return Objects.equals(buildDirectory, that.buildDirectory)
                && Objects.equals(installDirectory, that.installDirectory)
                && Objects.equals(defined, that.defined);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildDirectory);
    }

    public boolean isDefined() {
        return defined;
    }

    @SuppressWarnings("FieldHasSetterButNoGetter")
    public static class Builder {
        private String buildDirectory = null;
        private boolean defined = false;
        private String installDirectory = null;

        public Builder setBuildDirectory(String buildDirectory) {
            this.buildDirectory = buildDirectory;
            return this;
        }
        public Builder setDefined(boolean isDefined) {
            this.defined = isDefined;
            return this;
        }

        public Builder setInstallDirectory(String installDirectory) {
            this.installDirectory = installDirectory;
            return this;
        }

        public ConfigRootFacetProperties build() {
            return new ConfigRootFacetProperties(defined, buildDirectory, installDirectory);
        }


    }

}
