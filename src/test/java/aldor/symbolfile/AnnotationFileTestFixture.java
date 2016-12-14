package aldor.symbolfile;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AnnotationFileManager;
import aldor.util.VirtualFileTests;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AnnotationFileTestFixture {
    private final Map<String, VirtualFile> fileForName = Maps.newHashMap();
    @Nullable
    private Project project;

    AnnotationFileTestFixture(Project project) {
        this.project = project;
    }

    public AnnotationFileTestFixture() {
        project = null;
    }

    public VirtualFile createFile(String name, String text) {
        Assert.assertNotNull(project);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            VirtualFile file = VirtualFileTests.createFile(project.getBaseDir(), name, text);
            fileForName.put(name, file);
        });
        return fileForName.get(name);
    }

    void project(Project project) {
        this.project = project;
    }

    public VirtualFile fileForName(String name) {
        return fileForName.get(name);
    }

    public void compileFile(VirtualFile file) throws ExecutionException, InterruptedException {
        Assert.assertNotNull(project);
        List<Future<Void>> result = Lists.newArrayList();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            Module module = AldorModuleManager.getInstance(project).aldorModules().stream().findFirst().orElseThrow(()->new RuntimeException("no module for " + file));
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            AnnotationFileManager manager = AnnotationFileManager.getAnnotationFileManager(module);
            assert manager != null;

            Future<Void> fut = manager.requestRebuild(psiFile);
            result.add(fut);
        });

        result.get(0).get();
    }

}
