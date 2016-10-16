package aldor.build.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AldorModuleManager {
    private static final Key<AldorModuleManager> key = new Key<>(AldorModuleManager.class.getName());
    private final Project project;

    public AldorModuleManager(Project project) {
        this.project = project;
    }

    public static AldorModuleManager getInstance(Project project) {
        AldorModuleManager manager = project.getUserData(key);
        if (manager != null) {
            return manager;
        }
        //noinspection SynchronizationOnStaticField
        synchronized (key) {
            manager = project.getUserData(key);
            if (manager != null) {
                return manager;
            }
            manager = new AldorModuleManager(project);
            project.putUserData(key, manager);
            return manager;
        }
    }


    public Collection<Module> aldorModules() {
        Stream<Module> modules = Arrays.stream(ModuleManager.getInstance(project).getModules());

        modules = modules.filter(module -> ModuleType.get(module).equals(AldorModuleType.instance()));

        return modules.collect(Collectors.toList());
    }

    @NotNull
    public Optional<Pair<Module, VirtualFile>> aldorModuleForFile(@NotNull VirtualFile file) {
        Collection<Module> modules = aldorModules();
        Stream<Pair<Module, VirtualFile>> moduleStream = modules.stream()
                .flatMap(module -> Arrays.stream(ModuleRootManager.getInstance(module).getContentRoots())
                        .map(root -> Pair.create(module, root)))
                .filter(pair -> isParent(pair.getSecond(), file));

        return moduleStream.findFirst();
    }

    private boolean isParent(VirtualFile root, VirtualFile containingFile) {
        VirtualFile file = containingFile;
        while (true) {
            if (file == null) {
                return false;
            }
            if (Objects.equals(root, file)) {
                return true;
            }

            file = file.getParent();
        }
    }


    public String buildPathForFile(VirtualFile virtualFile) {
        Optional<Pair<Module, VirtualFile>> maybeModuleAndRoot = aldorModuleForFile(virtualFile);
        if (!maybeModuleAndRoot.isPresent()) {
            return null;
        }
        VirtualFile root = maybeModuleAndRoot.get().getSecond();

        return buildPathFromRoot(root, virtualFile.getParent());
    }

    private String buildPathFromRoot(VirtualFile root, VirtualFile virtualFile) {
        if (virtualFile.equals(root)) {
            return root.getPath();
        }
        else if (virtualFile.findChild("configure.ac") != null) {
            return root.getPath() + "/build";
        }
        else {
            return buildPathFromRoot(root, virtualFile.getParent()) + "/" + virtualFile.getName();
        }
    }

    public String annotationFileForSourceFile(VirtualFile file) {
        return buildPathForFile(file) + "/" + StringUtil.trimExtension(file.getName()) + ".abn";
    }

}
