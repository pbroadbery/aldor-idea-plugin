package aldor.builder.jps;

public class JpsAldorModuleProperties {
    private final String outputDirectory;
    private final JpsAldorMakeDirectoryOption makeDirectoryOption;

    public JpsAldorModuleProperties(String outputDirectory, JpsAldorMakeDirectoryOption option) {
        this.outputDirectory = outputDirectory;
        this.makeDirectoryOption = option;
    }

    public String outputDirectory() {
        return outputDirectory;
    }

    public JpsAldorMakeDirectoryOption makeDirectoryOption() {
        return makeDirectoryOption;
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
}
