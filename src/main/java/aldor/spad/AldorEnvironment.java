package aldor.spad;

import aldor.typelib.AxiomInterface;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;

import java.util.Collections;

public class AldorEnvironment implements SpadEnvironment {
    private final VirtualFile sdkDirectory;

    public AldorEnvironment(VirtualFile sdkDir) {
        this.sdkDirectory = sdkDir;
    }

    @Override
    public AxiomInterface create() {
        return AxiomInterface.createAldorLibrary(sdkDirectory.getPath(), Collections.emptyList());
    }

    @Override
    public String name() {
        return "AldorEnvironment: " + sdkDirectory;
    }

    @Override
    public GlobalSearchScope scope(Project project) {
        return GlobalSearchScopesCore.directoriesScope(project, true, sdkDirectory);
    }

    @Override
    public boolean containsFile(VirtualFile file) {
        if (VfsUtilCore.isAncestor(sdkDirectory, file, true)) {
            return true;
        }
        return false;
    }
}
