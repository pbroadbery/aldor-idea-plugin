package aldor.spad;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FricasSpadLibraryBuilder {
    private Project project = null;
    private VirtualFile daaseDirectory = null;
    private VirtualFile daaseSourceDirectory = null;
    private final List<VirtualFile> nrlibDirectories = new ArrayList<>();
    private final List<VirtualFile> nrlibSourceDirectories = new ArrayList<>();

    public FricasSpadLibraryBuilder project(Project project) {
        this.project = project;
        return this;
    }

    public FricasSpadLibraryBuilder daaseDirectory(@NotNull VirtualFile daaseDirectory) {
        return daaseDirectory(daaseDirectory, daaseDirectory.findFileByRelativePath("../src/algebra"));
    }

    public FricasSpadLibraryBuilder daaseDirectory(@NotNull VirtualFile daaseDirectory, VirtualFile daaseSourceDirectory) {
        this.daaseDirectory = daaseDirectory;
        this.daaseSourceDirectory = daaseSourceDirectory;
        return this;
    }

    public FricasSpadLibraryBuilder nrlibDirectory(@NotNull VirtualFile nrlibDirectory, @Nullable VirtualFile nrlibSourceDirectory) {
        this.nrlibDirectories.add(nrlibDirectory);
        if (nrlibSourceDirectory != null) {
            this.nrlibSourceDirectories.add(nrlibSourceDirectory);
        }
        return this;
    }

    public FricasEnvironment createFricasEnvironment() {
        return new FricasEnvironment(daaseDirectory, daaseSourceDirectory, nrlibDirectories, nrlibSourceDirectories);
    }

    public FricasSpadLibrary createFricasSpadLibrary() {
        return new FricasSpadLibrary(project, createFricasEnvironment());
    }
}
