package aldor.build.module;

import aldor.annotations.AnnotationFileNavigator;
import aldor.annotations.AnnotationFileNavigatorManager;
import aldor.util.AnnotatedOptional;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aldor.util.StringUtilsAldorRt.trimExtension;

public final class AldorModuleManager {
    private static final Logger LOG = Logger.getInstance(AldorModuleManager.class);
    private static final Key<AldorModuleManager> key = new Key<>(AldorModuleManager.class.getName());

    public AldorModuleManager(Project project) {

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


    public Collection<Module> aldorModules(Project project) {
        Stream<Module> modules = Arrays.stream(ModuleManager.getInstance(project).getModules());

        modules = modules.filter(module -> AldorModuleType.instance().is(module));

        return modules.collect(Collectors.toList());
    }


    @NotNull
    public AnnotatedOptional<AnnotationFileNavigator, String> annotationFileManagerForFile(Project project, @NotNull VirtualFile virtualFile) {
        AnnotationFileNavigator qq = AnnotationFileNavigatorManager.instance().getInstance(project);
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
    public String annotationFileForSourceFile(Project project, @NotNull VirtualFile file) {
        String buildPath = buildPathForFile(project, file);
        if (buildPath == null) {
            return null;
        }
        String result = buildPath + "/" + trimExtension(file.getName()) + ".abn";
        LOG.info("Annotation file for " + file + " --> " + result);
        return result;
    }

    @Nullable // TODO: Return a File
    public String buildPathForFile(Project project, @NotNull VirtualFile virtualFile) {
        Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(virtualFile);
        if (module == null) {
            return null;
        }
        AldorModuleFacade facade = AldorModuleFacade.forModule(module).orElse(null);
        if (facade == null) {
            return null;
        }

        return Optional.ofNullable(facade.buildDirectory(virtualFile)).map(x -> x.getPath()).orElse(null);
    }

    @Nullable
    public String annotationFileForSourceFile(PsiFileSystemItem file) {
        if (file.getVirtualFile() == null) {
            return null;
        }
        if (file.getVirtualFile().getFileSystem().getNioPath(file.getVirtualFile()) == null) {
            return null;
        }
        return annotationFileForSourceFile(file.getProject(), file.getVirtualFile());
    }

}
