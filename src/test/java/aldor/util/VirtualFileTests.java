package aldor.util;

import aldor.build.module.AldorModuleManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class VirtualFileTests {

    public static VirtualFile getProjectRoot(Project project) {
        AldorModuleManager mgr = AldorModuleManager.getInstance(project);
        List<VirtualFile> roots = mgr.aldorModules(project).stream()
                .flatMap(mod -> Arrays.stream(ModuleRootManager.getInstance(mod).getContentRoots()))
                .collect(Collectors.toList());

        return roots.get(0);
    }

    public static VirtualFile createFile(VirtualFile dir, String name, String content) {
        byte[] bytes = content.getBytes(Charset.defaultCharset());

        return createFile(dir, name, bytes);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static VirtualFile createFile(VirtualFile dir, String name, byte[] bytes) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                VirtualFile existingFile = dir.findChild(name);
                if (existingFile != null) {
                    existingFile.delete(null);
                }
                VirtualFile file = dir.createChildData(null, name);
                //noinspection NestedTryStatement
                try (OutputStream stream = file.getOutputStream(null)) {
                    stream.write(bytes);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        return dir.findChild(name);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static void writeFile(VirtualFile file, byte[] bytes) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                file.setBinaryContent(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static VirtualFile createChildDirectory(VirtualFile dir, String dirName) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                dir.createChildDirectory(null, dirName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return dir.findChild(dirName);
    }

    public static void deleteFile(VirtualFile file) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
            file.delete(null);
        } catch (IOException e) {
            e.printStackTrace();
        }});
    }
}
