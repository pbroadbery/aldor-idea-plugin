package aldor.builder.jps;

public class AldorModuleExtensionPropertiesBuilder {
    private String sdkName = null;
    private String outputDirectory = null;
    private JpsAldorMakeDirectoryOption option = JpsAldorMakeDirectoryOption.BuildRelative;
    private boolean buildJavaComponents = false;
    private String javaSdkName = null;

    public AldorModuleExtensionPropertiesBuilder() {

    }

    public AldorModuleExtensionPropertiesBuilder(AldorModuleExtensionProperties myProperties) {
        sdkName = myProperties.sdkName();
        outputDirectory = myProperties.outputDirectory();
        option = myProperties.makeDirectoryOption();
        buildJavaComponents = myProperties.isBuildJavaComponents();
        javaSdkName = myProperties.javaSdkName();
    }

    public AldorModuleExtensionPropertiesBuilder setSdkName(String sdkName) {
        this.sdkName = sdkName;
        return this;
    }

    public AldorModuleExtensionPropertiesBuilder setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public AldorModuleExtensionPropertiesBuilder setOption(JpsAldorMakeDirectoryOption option) {
        this.option = option;
        return this;
    }

    public AldorModuleExtensionPropertiesBuilder setBuildJavaComponents(boolean buildJavaComponents) {
        this.buildJavaComponents = buildJavaComponents;
        return this;
    }

    public AldorModuleExtensionPropertiesBuilder setJavaSdkName(String javaSdkName) {
        this.javaSdkName = javaSdkName;
        return this;
    }

    public AldorModuleExtensionProperties build() {
        return new AldorModuleExtensionProperties(sdkName, outputDirectory, option, buildJavaComponents, javaSdkName);
    }
}