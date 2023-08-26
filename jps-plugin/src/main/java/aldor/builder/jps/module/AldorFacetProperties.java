package aldor.builder.jps.module;

import aldor.builder.jps.SpadFacetProperties;
import com.google.common.base.Objects;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * TODO: Clean up relationship between outputDirectory and relativeOutputDirectory.
 * Configured builds: Only outputDirectory is needed.
 * Source: Use relativeOutput to find code
 * Build: ?
 */
public class AldorFacetProperties implements SpadFacetProperties {
    @Tag
    private final boolean buildJavaComponents;
    @Attribute
    private final String sdkName;
    @Attribute
    private final String javaSdkName;
    @Tag("aldorMakeConvention")
    private final MakeConvention makeConvention;
    @Nullable
    @Attribute("aldorOutputDirectory")
    private final String outputDirectory;
    @Attribute("aldorRelativeOutputDirectory")
    private final String relativeOutputDirectory;
    private final WithJava java;

    public AldorFacetProperties() {
        this(null, WithJava.Disabled, null, MakeConvention.Source, null, "");
    }

    private AldorFacetProperties(String sdkName, WithJava java,
                                String javaSdkName, MakeConvention makeConvention,
                                @Nullable String outputDirectory,
                                String relativeOutputDirectory) {
        this.sdkName = sdkName;
        this.java = java;
        this.buildJavaComponents = java.enabled();
        this.javaSdkName = javaSdkName;
        this.makeConvention = makeConvention;
        this.outputDirectory = outputDirectory;
        this.relativeOutputDirectory = relativeOutputDirectory;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder asBuilder() {
        return new Builder(this);
    }

    @Override
    public String sdkName() {
        return sdkName;
    }

    public boolean buildJavaComponents() {
        return isBuildJavaComponents();
    }

    public boolean isValid() {
        return true;
    }

    public boolean isBuildJavaComponents() {
        return buildJavaComponents;
    }

    public String javaSdkName() {
        return javaSdkName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        AldorFacetProperties other = (AldorFacetProperties) o;
        return (Objects.equal(sdkName, other.sdkName())) &&
                (buildJavaComponents == other.buildJavaComponents());
    }

    @Override
    public int hashCode() {
        int result = 7337;
        result = (31 * result) + Optional.ofNullable(sdkName).hashCode();
        result = (31 * result) + (buildJavaComponents ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AldorModuleExtensionProperties{" +
                ", java=" + buildJavaComponents +
                ", outputDirectory=" + outputDirectory +
                ", relativeOutputDirectory=" + relativeOutputDirectory +
                '}';
    }

    public String relativeOutputDirectory() {
        return relativeOutputDirectory;
    }

    public MakeConvention makeConvention() {
        return makeConvention;
    }

    public String outputDirectory() {
        return outputDirectory;
    }

    public enum WithJava {
        Enabled, Disabled;
        boolean enabled() {
            return this == Enabled;
        }
    }

    public static class Builder {
        private WithJava java = WithJava.Disabled;
        private String sdkName = null;
        private String javaSdkName = null;
        private MakeConvention makeConvention = MakeConvention.None;
        private String outputDirectory = ".";
        private String relativeOutputDirectory = "";

        public Builder() { }

        public Builder(AldorFacetProperties properties) {
            this.java = properties.java;
            this.sdkName = properties.sdkName;
            this.javaSdkName = properties.javaSdkName;
            this.makeConvention = properties.makeConvention;
            this.outputDirectory = properties.outputDirectory;
            this.relativeOutputDirectory = properties.relativeOutputDirectory;
        }

        public Builder java(WithJava java) {
            this.java = java;
            return this;
        }

        public Builder sdkName(String sdkName) {
            this.sdkName = sdkName;
            return this;
        }

        public Builder javaSdkName(String javaSdkName) {
            this.javaSdkName = javaSdkName;
            return this;
        }

        public Builder makeConvention(MakeConvention convention) {
            this.makeConvention = convention;
            return this;
        }

        public Builder outputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
            return this;
        }

        public Builder relativeOutputDirectory(String relativeOutputDirectory) {
            this.relativeOutputDirectory = relativeOutputDirectory;
            return this;
        }

        public AldorFacetProperties build() {
            return new AldorFacetProperties(sdkName, java, javaSdkName, makeConvention, outputDirectory, relativeOutputDirectory);
        }

    }
}
