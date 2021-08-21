package aldor.spad;

import aldor.typelib.AxiomInterface;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;

import java.util.Collections;
import java.util.List;

public class AldorEnvironment implements SpadEnvironment {
    private static final Logger LOG = Logger.getInstance(AldorEnvironment.class);
    private final VirtualFile sdkDirectory;

    public AldorEnvironment(VirtualFile sdkDir) {
        this.sdkDirectory = sdkDir;
    }

    @Override
    public AxiomInterface create() {
        LOG.info("AldorEnvironment::create " + sdkDirectory.getPath());
        String aldorPath = sdkDirectory.getPath() + "/share/aldor/lib/aldor";
        String algebraPath = sdkDirectory.getPath() + "/share/aldor/lib/algebra";
        AxiomInterface aldorLibInterface = AxiomInterface.createAldorLibrary(aldorPath, Collections.emptyList());
        AxiomInterface iface = AxiomInterface.createAldorLibrary(algebraPath, List.of(aldorLibInterface.env()));

        return iface;
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
