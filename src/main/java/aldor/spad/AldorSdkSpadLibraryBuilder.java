package aldor.spad;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class AldorSdkSpadLibraryBuilder {
    private final Project project;
    private final VirtualFile sdkDir;

    AldorSdkSpadLibraryBuilder(Project project, VirtualFile sdkDir) {
        this.project = project;
        this.sdkDir = sdkDir;
    }

    public FricasSpadLibrary build() {
        return new FricasSpadLibrary(project, createAldorEnvironment());
    }

    private SpadEnvironment createAldorEnvironment() {
        return new AldorEnvironment(this.sdkDir);
    }

}
