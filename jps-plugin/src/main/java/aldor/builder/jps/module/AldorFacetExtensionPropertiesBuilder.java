package aldor.builder.jps.module;

public class AldorFacetExtensionPropertiesBuilder {
    private String sdkName = null;
    private boolean buildJavaComponents = false;
    private String javaSdkName = null;
    private MakeConvention makeConvention = MakeConvention.Source;
    private String outputDirectory = null;
    private String relativeOutputDirectory = "";

    public AldorFacetExtensionPropertiesBuilder() {
    }

    public AldorFacetExtensionPropertiesBuilder(AldorFacetExtensionProperties myProperties) {
        sdkName = myProperties.sdkName();
        buildJavaComponents = myProperties.isBuildJavaComponents();
        javaSdkName = myProperties.javaSdkName();
    }

    public AldorFacetExtensionPropertiesBuilder setSdkName(String sdkName) {
        this.sdkName = sdkName;
        return this;
    }

    public AldorFacetExtensionPropertiesBuilder setBuildJavaComponents(boolean buildJavaComponents) {
        this.buildJavaComponents = buildJavaComponents;
        return this;
    }

    public AldorFacetExtensionPropertiesBuilder setJavaSdkName(String javaSdkName) {
        this.javaSdkName = javaSdkName;
        return this;
    }

    public AldorFacetExtensionPropertiesBuilder setMakeConvention(MakeConvention makeConvention) {
        this.makeConvention = makeConvention;
        return this;
    }

    public AldorFacetExtensionPropertiesBuilder setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public AldorFacetExtensionPropertiesBuilder setRelativeOutputDirectory(String relativeOutputDirectory) {
        this.relativeOutputDirectory = relativeOutputDirectory;
        return this;
    }

    public AldorFacetExtensionProperties build() {
        return new AldorFacetExtensionProperties(sdkName,
                buildJavaComponents ? AldorFacetExtensionProperties.WithJava.Enabled: AldorFacetExtensionProperties.WithJava.Disabled,
                javaSdkName,
                makeConvention, outputDirectory, relativeOutputDirectory);
    }
}