package aldor.spad;

import aldor.build.module.AldorModuleType;
import aldor.language.AldorLanguage;
import aldor.sdk.AldorSdkType;
import aldor.sdk.AxiomSdk;
import aldor.sdk.FricasSdkType;
import aldor.sdk.SdkTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.CompilerModuleExtension;
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
        if (module.getUserData(key) != null) {
            return module.getUserData(key);
        }

        SpadLibrary lib = forModule0(module);
        module.putUserData(key, lib);
        return lib;
    }

    @Nullable
    private SpadLibrary forModule0(Module module) {
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        Sdk moduleSdk = rootManager.getSdk();
        if (moduleSdk == null) {
            return forProject(module.getProject());
        }
        if (SdkTypes.isLocalSdk(moduleSdk)) {
            VirtualFile path = rootManager.getModuleExtension(CompilerModuleExtension.class).getCompilerOutputPath();
            VirtualFile algebraPath = (path == null) ? null : path.findFileByRelativePath("src/algebra");
            if (algebraPath == null) {
                return null;
            }
            VirtualFile likelySourceDirectory = Optional.ofNullable((rootManager.getSourceRoots().length < 1) ? null : rootManager)
                    .map(mgr -> mgr.getSourceRoots()[0])
                    .flatMap(root -> Optional.ofNullable(root.findFileByRelativePath("algebra")))
                    .orElse(null);
            return forNRLibDirectory(module.getProject(), algebraPath, likelySourceDirectory);
        }
        else if (AldorModuleType.instance().is(module)) {
            return forAldorModule(module);
        }
        else {
            return forSdk(module.getProject(), moduleSdk);
        }
    }

    public static SpadLibraryManager instance() {
        return instance;
    }

    public void spadLibraryForSdk(@SuppressWarnings("TypeMayBeWeakened") @NotNull Sdk sdk, SpadLibrary spadLibrary) {
        sdk.putUserData(key, spadLibrary);
    }

    @Nullable
    private SpadLibrary forAldorModule(Module module) {
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        SpadLibrary sdkLibrary = Optional.ofNullable(rootManager.getSdk()).map(sdk -> forSdk(module.getProject(), sdk)).orElse(forProject(module.getProject()));
        VirtualFile[] roots = rootManager.getSourceRoots();
        if (roots.length == 0) {
            return sdkLibrary;
        }
        else {
            return new AldorModuleSpadLibraryBuilder(module).rootDirectory(roots[0]).dependency(sdkLibrary).createFricasSpadLibrary();
        }
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
    public SpadLibrary forSdk(Project project, @NotNull Sdk sdk) {
        if (sdk.getUserData(key) != null) {
            return sdk.getUserData(key);
        }
        SpadLibrary lib = doForSdk(project, sdk);
        if (lib == null) {
            return null;
        }
        sdk.putUserData(key, lib);
        return lib;
    }

    @Nullable
    private SpadLibrary doForSdk(Project project, @NotNull Sdk sdk) {
        if (!(sdk.getSdkType() instanceof AxiomSdk)) {
            return null;
        }
        SdkType sdkType = (SdkType) sdk.getSdkType();
        if (sdkType instanceof AldorSdkType) {
            return new AldorSdkSpadLibraryBuilder(project, sdk.getHomeDirectory()).createFricasSpadLibrary();
        }
        else if (sdkType instanceof FricasSdkType) {
            VirtualFile algebra = SdkTypes.algebraPath(sdk);
            SpadLibrary lib = null;
            if (algebra != null) {
                lib = new FricasSpadLibraryBuilder().project(project).daaseDirectory(algebra).createFricasSpadLibrary();
            }
            return lib;
        }
        else {
            return null;
        }
    }

    @Nullable
    public SpadLibrary forNRLibDirectory(@NotNull Project project, @NotNull VirtualFile directory, @Nullable VirtualFile sourceDirectory) {
        return new FricasSpadLibraryBuilder().project(project).nrlibDirectory(directory, sourceDirectory).createFricasSpadLibrary();
    }


    @Nullable
    public SpadLibrary spadLibraryForElement(PsiElement psiElement) {
        Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
        if (!psiElement.getContainingFile().getLanguage().isKindOf(AldorLanguage.INSTANCE)) {
            return null;
        }
        if (module != null) {
            SpadLibrary library = forModule(module);
            if (library != null) {
                return library;
            }
        }

        VirtualFile file = psiElement.getContainingFile().getVirtualFile();
        if (file == null) {
            return null;
        }
        DirectoryInfo info = DirectoryIndex.getInstance(psiElement.getProject()).getInfoForFile(file);
        if (info.isInLibrarySource(file)) {
            for (Sdk sdk : ProjectJdkTable.getInstance().getAllJdks()) {
                if (!(sdk.getSdkType() instanceof AxiomSdk)) {
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
