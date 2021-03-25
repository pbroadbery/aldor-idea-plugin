package aldor.builder.jps;

import com.google.common.base.Objects;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

import javax.annotation.Nonnull;
import java.util.Optional;

public class AldorModuleExtensionProperties implements SpadFacetProperties {
    @Tag
    @Nonnull
    private final String outputDirectory;
    @Tag
    private final JpsAldorMakeDirectoryOption makeDirectoryOption;
    @Tag
    private final boolean buildJavaComponents;
    @Attribute
    private final String sdkName;
    @Attribute
    private final String javaSdkName;

    public AldorModuleExtensionProperties() {
        this(null, "out/ao", JpsAldorMakeDirectoryOption.Source, WithJava.Disabled, null);
    }

    public AldorModuleExtensionProperties(String sdkName,
                                          @Nonnull String outputDirectory,
                                          JpsAldorMakeDirectoryOption option,
                                          WithJava java,
                                          String javaSdkName) {
        this.sdkName = sdkName;
        this.outputDirectory = outputDirectory;
        this.makeDirectoryOption = option;
        this.buildJavaComponents = java.enabled();
        this.javaSdkName = javaSdkName;
    }

    public static AldorModuleExtensionPropertiesBuilder builder() {
        return new AldorModuleExtensionPropertiesBuilder();
    }

    public AldorModuleExtensionPropertiesBuilder asBuilder() {
        return new AldorModuleExtensionPropertiesBuilder(this);
    }

    @Override
    public String sdkName() {
        return sdkName;
    }

    public String outputDirectory() {
        return outputDirectory;
    }

    public JpsAldorMakeDirectoryOption makeDirectoryOption() {
        return makeDirectoryOption;
    }

    public boolean buildJavaComponents() {
        return isBuildJavaComponents();
    }

    public boolean isValid() {
        if (makeDirectoryOption == JpsAldorMakeDirectoryOption.Invalid) {
            return false;
        }
        if ((outputDirectory == null) || outputDirectory.isEmpty()) {
            return false;
        }
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
        AldorModuleExtensionProperties other = (AldorModuleExtensionProperties) o;
        return (Objects.equal(sdkName, other.sdkName())) &&
                (buildJavaComponents == other.buildJavaComponents()) &&
                Objects.equal(outputDirectory, other.outputDirectory()) &&
                (makeDirectoryOption == other.makeDirectoryOption());
    }

    @Override
    public int hashCode() {
        int result = outputDirectory.hashCode();
        result = (31 * result) + Optional.ofNullable(sdkName).hashCode();
        result = (31 * result) + makeDirectoryOption.hashCode();
        result = (31 * result) + (buildJavaComponents ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AldorModuleExtensionProperties{" +
                "dir'" + outputDirectory + '\'' +
                ", opt=" + makeDirectoryOption +
                ", java=" + buildJavaComponents +
                '}';
    }

    public enum WithJava {
        Enabled, Disabled;
        boolean enabled() {
            return this == Enabled;
        }
    }
}
