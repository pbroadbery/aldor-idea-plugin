package aldor.spad;

import aldor.typelib.AxiomInterface;
import aldor.typelib.SymbolDatabase;
import aldor.typelib.SymbolDatabaseHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FricasEnvironment implements SpadEnvironment {
    public static final GlobalSearchScope[] EMPTY_GLOBAL_SEARCH_SCOPES = new GlobalSearchScope[0];
    @Nullable
    private final VirtualFile daaseDirectory;
    private final List<VirtualFile> nrLibs;
    @Nullable
    private final VirtualFile daaseSourceDirectory;
    private final List<VirtualFile> nrlibSourceDirectories;

    FricasEnvironment(@Nullable VirtualFile daaseDirectory, @Nullable VirtualFile daaseSourceDirectory, List<VirtualFile> nrLibs, List<VirtualFile> nrlibSourceDirectories) {
        this.daaseDirectory = daaseDirectory;
        this.nrLibs = new ArrayList<>(nrLibs);
        this.daaseSourceDirectory = daaseSourceDirectory;
        this.nrlibSourceDirectories = new ArrayList<>(nrlibSourceDirectories);
    }

    @Override
    public AxiomInterface create() {
        List<SymbolDatabase> databases = new ArrayList<>();
        if (daaseDirectory != null) {
            databases.add(SymbolDatabase.daases(daaseDirectory.getPath()));
        }
        databases.addAll(nrLibs.stream().map(dir -> SymbolDatabaseHelper.nrlib(dir.getPath())).collect(Collectors.toList()));
        if (databases.size() != 1) {
            throw new IllegalStateException("Invalid fricas library state - can only support one lib at the moment");
        }
        // Being lazy here - AxiomInterface should support multiple databases
        return AxiomInterface.create(databases.get(0));
    }

    @Override
    public String name() {
        return "FricasEnv: " + daaseDirectory + " " + nrLibs;
    }

    @Override
    public GlobalSearchScope scope(Project project) {
        List<GlobalSearchScope> lst = new ArrayList<>();
        if (daaseSourceDirectory != null) {
            lst.add(GlobalSearchScopesCore.directoriesScope(project, true, daaseSourceDirectory));
        }
        for (VirtualFile nrlibDir: nrlibSourceDirectories) {
            lst.add(GlobalSearchScopesCore.directoriesScope(project, true, nrlibDir));
        }
        if (lst.isEmpty()) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }
        return GlobalSearchScope.union(lst.toArray(EMPTY_GLOBAL_SEARCH_SCOPES));
    }

    @Override
    public boolean containsFile(VirtualFile file) {
        if ((daaseDirectory != null) && VfsUtilCore.isAncestor(daaseDirectory, file, true)) {
            return true;
        }
        if (nrLibs.stream().anyMatch(lib -> VfsUtilCore.isAncestor(lib, file, true))) {
            return true;
        }
        return false;
    }
}
