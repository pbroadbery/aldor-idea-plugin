package aldor.spad;

import aldor.language.SpadLanguage;
import aldor.sdk.FricasSdkType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.roots.impl.DirectoryInfo;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class SpadLibraryManager {
    private static final SpadLibraryManager instance = new SpadLibraryManager();
    private static final Key<SpadLibrary> key = new Key<>(SpadLibrary.class.getName());

    @Nullable
    public SpadLibrary forModule(Module module) {
        return forSdk(module.getProject(), ModuleRootManager.getInstance(module).getSdk());
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

        return forSdk(project, projectSdk);
    }

    @Nullable
    public SpadLibrary forSdk(Project project, Sdk sdk) {
        if (sdk.getUserData(key) != null) {
            return sdk.getUserData(key);
        }
        FricasSpadLibrary lib = new FricasSpadLibrary(project, sdk.getHomeDirectory());
        sdk.putUserData(key, lib);
        return lib;
    }


    @Nullable
    public SpadLibrary spadLibraryForElement(PsiElement psiElement) {
        Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
        if (!psiElement.getContainingFile().getLanguage().is(SpadLanguage.INSTANCE)) {
            return null;
        }
        if (module != null) {
            return forModule(module);
        }

        DirectoryInfo info = DirectoryIndex.getInstance(psiElement.getProject()).getInfoForFile(psiElement.getContainingFile().getVirtualFile());
        if (info.isInLibrarySource(psiElement.getContainingFile().getVirtualFile())) {
            for (Sdk sdk : ProjectJdkTable.getInstance().getAllJdks()) {
                if (!(sdk.getSdkType() instanceof FricasSdkType)) {
                    continue;
                }
                Optional<VirtualFile> any = Arrays.stream(sdk.getRootProvider().getFiles(OrderRootType.SOURCES))
                                                    .filter(r -> Objects.equals(r, info.getSourceRoot())).findAny();
                if (any.isPresent()) {
                    return forSdk(psiElement.getProject(), sdk);
                }
            }
        }
        return forProject(psiElement.getProject());
    }

}
