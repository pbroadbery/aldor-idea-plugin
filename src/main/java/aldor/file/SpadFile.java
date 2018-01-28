package aldor.file;

import aldor.language.SpadLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class SpadFile extends AxiomFile {
    public SpadFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, SpadLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return SpadFileType.INSTANCE;
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
