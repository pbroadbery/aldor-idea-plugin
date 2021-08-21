package aldor.spad;

import aldor.build.facet.SpadFacet;
import aldor.build.module.AldorModuleType;
import aldor.builder.jps.SpadFacetProperties;
import aldor.language.AldorLanguage;
import aldor.sdk.AxiomSdk;
import aldor.sdk.SdkTypes;
import aldor.sdk.aldor.AldorSdkType;
import aldor.sdk.fricas.FricasSdkType;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.roots.impl.DirectoryInfo;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class SpadLibraryManager implements Disposable {
    private static final Logger LOG = Logger.getInstance(SpadLibraryManager.class);

    private static final Key<SpadLibraryManager> managerKey = new Key<>(SpadLibraryManager.class.getName());
    private final SdkLibraryContainer container = new SdkLibraryContainer();
    private final Project project;

    private SpadLibraryManager(Project project) {
        this.project = project;
    }

    public static SpadLibraryManager getInstance(Project project) {
        return project.getService(SpadLibraryManager.class);
    }

    public static AldorModuleSpadLibraryManager getInstance(Module module) {
        return module.getService(AldorModuleSpadLibraryManager.class);
    }

    @Nullable
    public SpadLibrary forModule(Module module, @NotNull FileType fileType) {
        assert !module.getProject().isDisposed();
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        Sdk moduleSdk = Optional.ofNullable(AldorModuleType.instance().facetModuleType(module, fileType))
                .flatMap(SpadFacet::getProperties)
                .map(SpadFacetProperties::sdkName)
                .map(name -> ProjectJdkTable.getInstance().findJdk(name))
                .orElse(null);

        if (moduleSdk == null) {
            moduleSdk = rootManager.getSdk();
            if (!SdkTypes.isAxiomSdk(moduleSdk)) {
                moduleSdk = null;
            }
        }

        if (moduleSdk == null) {
            return forProject(module.getProject());
        }

        if (SdkTypes.isLocalSdk(moduleSdk)) {
            VirtualFile path = rootManager.getModuleExtension(CompilerModuleExtension.class).getCompilerOutputPath(); // FIXME - use facet
            VirtualFile algebraPath = (path == null) ? null : path.findFileByRelativePath("src/algebra");
            if (algebraPath == null) {
                return null;
            }
            VirtualFile likelySourceDirectory = Optional.ofNullable((rootManager.getSourceRoots().length < 1) ? null : rootManager)
                    .map(mgr -> mgr.getSourceRoots()[0])
                    .flatMap(root -> Optional.ofNullable(root.findFileByRelativePath("algebra")))
                    .orElse(null);
            @Nullable FricasSpadLibrary lib = createNRLibDirectory(module.getProject(), algebraPath, likelySourceDirectory);
            if (lib != null) {
                getInstance(module).register(lib);
                return lib;
            }
            return null;
        }
        else if (AldorModuleType.instance().is(module)) {
            return forAldorModule(module, moduleSdk);
        }
        else {
            return forSdk(moduleSdk);
        }
    }

    @Nullable
    private SpadLibrary forAldorModule(Module module, Sdk moduleSdk) {
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        SpadLibrary sdkLibrary = forSdk(moduleSdk);
        VirtualFile[] roots = rootManager.getSourceRoots();
        if (roots.length == 0) {
            return sdkLibrary;
        }
        else {
            FricasSpadLibrary lib = new AldorModuleSpadLibraryBuilder(module).rootDirectory(roots[0]).dependency(sdkLibrary).createFricasSpadLibrary();
            Disposer.register(getInstance(module), lib);
            return lib;
        }
    }

    @Nullable
    public SpadLibrary forProject(Project project) {
        LOG.warn("No default library for project as a whole");
        return null;
    }

    @Nullable
    public SpadLibrary forSdk(@NotNull Sdk sdk) {
        Optional<SpadLibrary> libraryMaybe = container.find(sdk);
        if (libraryMaybe.isPresent()) {
            return libraryMaybe.get();
        }
        SpadLibrary lib = doForSdk(project, sdk);
        if (lib == null) {
            return null;
        }
        lib = container.putIfAbsent(sdk, lib);
        return lib;
    }

    @Nullable
    private SpadLibrary doForSdk(Project project, @NotNull Sdk sdk) {
        if (!(sdk.getSdkType() instanceof AxiomSdk)) {
            return null;
        }
        SdkType sdkType = (SdkType) sdk.getSdkType();
        if (sdkType instanceof AldorSdkType) {
            FricasSpadLibrary lib = new AldorSdkSpadLibraryBuilder(project, sdk.getHomeDirectory()).build();
            Disposer.register(this, lib);
            return lib;
        }
        else if (sdkType instanceof FricasSdkType) {
            VirtualFile algebra = SdkTypes.algebraPath(sdk);
            FricasSpadLibrary lib = null;
            if (algebra != null) {
                lib = new FricasSpadLibraryBuilder().project(project).daaseDirectory(algebra).createFricasSpadLibrary();
                Disposer.register(this, lib);
            }
            return lib;
        }
        else {
            return null;
        }
    }

    @Nullable
    @VisibleForTesting
    public FricasSpadLibrary createNRLibDirectory(@NotNull Project project, @NotNull VirtualFile directory, @Nullable VirtualFile sourceDirectory) {
        return new FricasSpadLibraryBuilder().project(project).nrlibDirectory(directory, sourceDirectory).createFricasSpadLibrary();
    }


    @Nullable
    public SpadLibrary spadLibraryForElement(PsiElement psiElement) {
        Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
        if (!psiElement.getContainingFile().getLanguage().isKindOf(AldorLanguage.INSTANCE)) {
            return null;
        }
        if (!psiElement.getContainingFile().getLanguage().is(AldorLanguage.INSTANCE)) {
            SpadLibrary lib = forAldorFile(psiElement.getContainingFile(), module);
            if (lib != null) {
                return lib;
            }
        }
        if (module != null) {
            SpadLibrary library = forModule(module, psiElement.getContainingFile().getFileType());
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
                    return forSdk(sdk);
                }
            }
        }
        return forProject(psiElement.getProject());
    }

    private SpadLibrary forAldorFile(PsiFile file, Module module) {
        return null;
    }

    @Override
    public void dispose() {
        container.dispose();
    }

    private static class SdkLibraryContainer {
        private final Map<Sdk, SpadLibrary> libraryForSdk = new ConcurrentHashMap<>();

        Optional<SpadLibrary> find(Sdk sdk) {
            return Optional.ofNullable(libraryForSdk.get(sdk));
        }

        SpadLibrary putIfAbsent(Sdk sdk, SpadLibrary spadLibrary) {
            SpadLibrary oldValue = libraryForSdk.putIfAbsent(sdk, spadLibrary);
            if (oldValue != null) {
                return oldValue;
            }
            return spadLibrary;
        }

        public void dispose() {
            LOG.info("Disposing SDK container.. " + libraryForSdk.size());
            for (Sdk sdk: libraryForSdk.keySet()) {
                LOG.info("SDK: " + sdk + " path: " + sdk.getHomePath());
            }
            libraryForSdk.clear();
        }
    }

}
