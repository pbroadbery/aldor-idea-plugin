package aldor.build.module;

import aldor.util.AnnotatedOptional;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

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
    public AnnotatedOptional<AnnotationFileManager, String> annotationFileManagerForFile(@NotNull VirtualFile virtualFile) {
        AnnotationFileManager qq = AnnotationFileManager.getAnnotationFileManager(project);
        return AnnotatedOptional.of(qq);
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

    @Nullable
    public String annotationFilePathForFile(@NotNull VirtualFile virtualFile) {
        return ofNullable(buildPathForFile(virtualFile)).map(p -> p+"/"+virtualFile.getName()).orElse(null);
    }

    @Nullable
    public String buildPathForFile(@NotNull VirtualFile virtualFile) {
        VirtualFile root = ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(virtualFile);
        if (root == null) {
            return virtualFile.getParent().getPath();
        }
        return buildPathFromRoot(root, virtualFile.getParent());
    }

    private String buildPathFromRoot(@NotNull VirtualFile root, @NotNull VirtualFile virtualFile) {
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

    @Nullable
    public String annotationFileForSourceFile(PsiFileSystemItem file) {
        if (file.getVirtualFile() == null) {
            return null;
        }
        return annotationFileForSourceFile(file.getVirtualFile());
    }

    public String annotationFileForSourceFile(@NotNull VirtualFile file) {
        return buildPathForFile(file) + "/" + StringUtil.trimExtension(file.getName()) + ".abn";
    }
}
