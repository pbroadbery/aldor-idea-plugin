package aldor.build;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
public abstract class AldorCompilationService {
    private static final Logger LOG = Logger.getInstance(AldorCompilationService.class);

    public static AldorCompilationService getAldorCompilationService(Project project) {
        return project.getService(AldorCompilationService.class);
    }

    public abstract void compilationResultsFor(VirtualFile file);
}
