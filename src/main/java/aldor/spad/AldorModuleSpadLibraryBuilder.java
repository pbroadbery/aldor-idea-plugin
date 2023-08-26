package aldor.spad;

import aldor.typelib.AxiomInterface;
import aldor.typelib.Env;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AldorModuleSpadLibraryBuilder {
    private final Module module;
    private final List<SpadLibrary> dependencies = new ArrayList<>();
    private VirtualFile sourceDirectory = null;
    private File buildPath = null;

    public AldorModuleSpadLibraryBuilder(Module module) {
        this.module = module;
    }

    public AldorModuleSpadLibraryBuilder sourceRootDirectory(@NotNull VirtualFile sourceRootDirectory) {
        this.sourceDirectory = sourceRootDirectory;
        if ((buildPath == null) && (sourceRootDirectory.getFileSystem().getNioPath(sourceRootDirectory) != null)) {
            buildPath = sourceRootDirectory.toNioPath().toFile();
        }
        return this;
    }

    public AldorModuleSpadLibraryBuilder buildPath(File buildPath) {
        this.buildPath = buildPath;
        return this;
    }

    public FricasSpadLibrary createFricasSpadLibrary() {
        FricasSpadLibrary lib = new FricasSpadLibrary(module.getProject(), createAldorEnvironment());
        for (SpadLibrary dep: dependencies) {
            dep.addDependant(lib);
        }
        return lib;
    }

    public AldorModuleSpadLibraryBuilder dependency(SpadLibrary sdkLibrary) {
        dependencies.add(sdkLibrary);
        return this;
    }

    private SpadEnvironment createAldorEnvironment() {
        return new AldorModuleEnvironment(module.getName(), sourceDirectory, buildPath, dependencies);
    }

    public static class AldorModuleEnvironment implements SpadEnvironment {
        private static final Logger LOG = Logger.getInstance(AldorModuleEnvironment.class);
        private final String name;
        private final VirtualFile root;
        private final File buildRoot;
        private final List<SpadLibrary> dependencies;

        public AldorModuleEnvironment(String name,
                                      @NotNull VirtualFile sourceRoot,
                                      @Nullable File buildRoot,
                                      List<SpadLibrary> dependencies) {
            this.name = name;
            this.root = sourceRoot;
            this.buildRoot = buildRoot;
            this.dependencies = dependencies;
        }

        @Override
        public AxiomInterface create() {
            List<Env> deps = dependencies.stream()
                    .map(SpadLibrary::environment).toList();
            if (buildRoot == null) {
                return AxiomInterface.createAldorLibrary(deps);
            } else {
                return AxiomInterface.createAldorLibrary(buildRoot.getAbsolutePath(), dependencies.stream()
                        .map(SpadLibrary::environment).collect(Collectors.toList()));
            }
        }

        @Override
        public String name() {
            return "Aldor Module: " + name;
        }

        @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
        @Override
        public GlobalSearchScope scope(Project project) {
            if (dependencies.isEmpty()) {
                return GlobalSearchScopesCore.directoriesScope(project, true, root);
            }
            GlobalSearchScope[] arr = new GlobalSearchScope[1+dependencies.size()];
            int i=0;
            arr[i++] = GlobalSearchScopesCore.directoriesScope(project, true, root);
            for (SpadLibrary lib: dependencies) {
                arr[i++] = lib.scope(project);
            }
            return GlobalSearchScope.union(arr);
        }

        @Override
        public boolean containsBuildFile(VirtualFile file) {
            // TODO: This should consider case of buildRoot != sourceRoot.
            if (buildRoot == null) {
                return false;
            }
            if (file.getFileSystem().getNioPath(file) == null) {
                return false;
            }
            return VfsUtilCore.isAncestor(buildRoot, file.toNioPath().toFile(), true);
        }
    }
}
