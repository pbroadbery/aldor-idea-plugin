package aldor.builder.jps.module;

import aldor.builder.jps.SpadFacetProperties;
import com.google.common.base.Objects;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AldorFacetExtensionProperties implements SpadFacetProperties {
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

    public AldorFacetExtensionProperties() {
        this(null, WithJava.Disabled, null, MakeConvention.Source, null, "");
    }

    public AldorFacetExtensionProperties(String sdkName, WithJava java,
                                         String javaSdkName, MakeConvention makeConvention,
                                         @Nullable String outputDirectory, String relativeOutputDirectory) {
        this.sdkName = sdkName;
        this.buildJavaComponents = java.enabled();
        this.javaSdkName = javaSdkName;
        this.makeConvention = makeConvention;
        this.outputDirectory = outputDirectory;
        this.relativeOutputDirectory = relativeOutputDirectory;
    }

    public static AldorFacetExtensionPropertiesBuilder builder() {
        return new AldorFacetExtensionPropertiesBuilder();
    }

    public AldorFacetExtensionPropertiesBuilder asBuilder() {
        return new AldorFacetExtensionPropertiesBuilder(this);
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
        AldorFacetExtensionProperties other = (AldorFacetExtensionProperties) o;
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
}
