package aldor.spad;

import aldor.typelib.AxiomInterface;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;

public interface SpadEnvironment {
    AxiomInterface create();

    String name();

    /** Lookup for identifiers */
    GlobalSearchScope scope(Project project);

    /* Used to determine changed files */
    boolean containsBuildFile(VirtualFile file);
}
