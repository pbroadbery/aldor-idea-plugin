package aldor.build.module;

import com.intellij.util.xmlb.annotations.Attribute;

public class AldorModuleState {
    @Attribute
    private String outputDirectory;
    @Attribute
    private AldorMakeDirectoryOption makeDirectory;
    private long modificationCount = 0;

    public AldorModuleState() {
        this("", null);
    }

    public AldorModuleState(String outputDirectory, AldorMakeDirectoryOption makeDirectory) {
        this.outputDirectory = outputDirectory;
        this.makeDirectory = makeDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        modificationCount++;
        this.outputDirectory = outputDirectory;
    }

    public void setMakeDirectory(AldorMakeDirectoryOption option) {
        modificationCount++;
        this.makeDirectory = option;
    }

    public long getModificationCount() {
        return modificationCount;
    }

}
