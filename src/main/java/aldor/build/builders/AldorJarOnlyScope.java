package aldor.build.builders;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.ExportableUserDataHolderBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AldorJarOnlyScope extends ExportableUserDataHolderBase implements CompileScope {
    private final Module module;
    private final List<VirtualFile> sourceRoots;
    private boolean allRoots;

    public AldorJarOnlyScope(Module module, @Nullable Iterable<VirtualFile> files) {
        this.module = module;
        sourceRoots = new ArrayList<>();
        if (files == null) {
            allRoots = true;
        } else {
            allRoots = false;
            ApplicationManager.getApplication().runReadAction(
                    () -> {
                        for (VirtualFile file : files) {
                            assert file != null;
                            sourceRoots.add(file);
                        }
                    }
            );
        }
    }

    @NotNull
    @Override
    public VirtualFile[] getFiles(@Nullable FileType fileType, boolean inSourceOnly) {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    public boolean belongs(String url) {
        return false;
    }

    @NotNull
    @Override
    public Module[] getAffectedModules() {
        return Module.EMPTY_ARRAY;
    }

    public boolean isAllRoots() {
        return allRoots;
    }

    public VirtualFile[] sourceRoots() {
        return sourceRoots.toArray(VirtualFile.EMPTY_ARRAY);
    }

    public Module module() {
        return module;
    }
}
