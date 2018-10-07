package aldor.spad;

import aldor.typelib.AxiomInterface;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AldorModuleSpadLibraryBuilder {
    private final Module module;
    private final List<SpadLibrary> dependencies = new ArrayList<>();
    private VirtualFile rootDirectory = null;

    public AldorModuleSpadLibraryBuilder(Module module) {
        this.module = module;
    }

    public AldorModuleSpadLibraryBuilder rootDirectory(@NotNull VirtualFile rootDirectory) {
        this.rootDirectory = rootDirectory;
        return this;
    }

    public FricasSpadLibrary createFricasSpadLibrary() {
        FricasSpadLibrary lib = new FricasSpadLibrary(module.getProject(), createAldorEnvironment());
        for (SpadLibrary dep: dependencies) {
            dep.addDependant(lib);
        }
        return lib;
    }

    private SpadEnvironment createAldorEnvironment() {
        return new AldorModuleEnvironment(module.getName(), rootDirectory, dependencies);
    }

    public AldorModuleSpadLibraryBuilder dependency(SpadLibrary sdkLibrary) {
        dependencies.add(sdkLibrary);
        return this;
    }

    public static class AldorModuleEnvironment implements SpadEnvironment {
        private static final Logger LOG = Logger.getInstance(AldorModuleEnvironment.class);
        private final String name;
        private final VirtualFile root;
        private final List<SpadLibrary> dependencies;

        public AldorModuleEnvironment(String name, @NotNull VirtualFile rootDirectory, List<SpadLibrary> dependencies) {
            this.name = name;
            this.root = rootDirectory;
            this.dependencies = dependencies;
        }

        @Override
        public AxiomInterface create() {
            LOG.info("Creating aldor library: " + name + " " + root + " " + dependencies);
            return AxiomInterface.createAldorLibrary(root.getPath(), dependencies.stream()
                    .map(SpadLibrary::environment).collect(Collectors.toList()));
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
        public boolean containsFile(VirtualFile file) {
            return VfsUtilCore.isAncestor(root, file, true);
        }
    }
}
