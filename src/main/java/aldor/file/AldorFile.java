package aldor.file;

import aldor.language.AldorLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class AldorFile extends AxiomFile {
    public AldorFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, AldorLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return AldorFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Aldor File";
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }


}
