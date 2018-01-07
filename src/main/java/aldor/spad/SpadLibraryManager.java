package aldor.spad;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpadLibraryManager {
    private static final SpadLibraryManager instance = new SpadLibraryManager();
    private static final Key<SpadLibrary> key = new Key<>(SpadLibrary.class.getName());

    @Nullable
    public SpadLibrary forModule(Module module) {
        return forProject(module.getProject());
    }

    public static SpadLibraryManager instance() {
        return instance;
    }

    public void spadLibraryForSdk(@SuppressWarnings("TypeMayBeWeakened") @NotNull Sdk sdk, @NotNull SpadLibrary spadLibrary) {
        sdk.putUserData(key, spadLibrary);
    }

    @Nullable
    public SpadLibrary forProject(Project project) {
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();

        if (projectSdk == null) {
            return null;
        }
        if (projectSdk.getUserData(key) != null) {
            return projectSdk.getUserData(key);
        }
        TestSpadLibrary lib = new TestSpadLibrary(project, null, projectSdk.getHomeDirectory());
        projectSdk.putUserData(key, lib);
        return lib;
    }

    @Nullable
    public SpadLibrary spadLibraryForElement(PsiElement psiElement) {
        Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
        if (module == null) {
            return forProject(psiElement.getProject());
        }
        return forModule(module);
    }

}
