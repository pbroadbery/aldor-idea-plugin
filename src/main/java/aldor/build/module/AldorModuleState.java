package aldor.build.module;

import com.intellij.util.xmlb.annotations.Attribute;

@Deprecated
public class AldorModuleState {
    @Attribute
    private String outputDirectory;
    @Attribute
    private AldorMakeDirectoryOption makeDirectory; // DO NOT USE - Take from SDK
    @Attribute
    private boolean buildJavaComponents;

    private long modificationCount = 0;

    public AldorModuleState() {
        this("", null, true);
    }

    public AldorModuleState(String outputDirectory, AldorMakeDirectoryOption makeDirectory, boolean buildJavaComponents) {
        this.outputDirectory = outputDirectory;
        this.makeDirectory = makeDirectory;
        this.buildJavaComponents = buildJavaComponents;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.modificationCount++;
        this.outputDirectory = outputDirectory;
    }

    public void setMakeDirectory(AldorMakeDirectoryOption option) {
        this.modificationCount++;
        this.makeDirectory = option;
    }

    public long getModificationCount() {
        return modificationCount;
    }

    public void setBuildJavaComponents(boolean flg) {
        this.modificationCount++;
        this.buildJavaComponents = flg;
    }

}
