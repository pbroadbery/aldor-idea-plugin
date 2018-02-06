package aldor.file;

import aldor.language.SpadLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class SpadFile extends AxiomFile {
    private FileType myType = null;

    public SpadFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, SpadLanguage.INSTANCE);
    }

    @Override
    @NotNull
    public FileType getFileType() {
        if (myType == null) {
            VirtualFile virtualFile = getOriginalFile().getVirtualFile();
            myType = (virtualFile == null)
                    ? FileTypeRegistry.getInstance().getFileTypeByFileName(getName())
                    : virtualFile.getFileType();
        }
        return myType;
    }

    @Override
    public String toString() {
        return "Spad File";
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }

}
