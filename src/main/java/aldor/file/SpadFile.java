package aldor.file;

import aldor.language.SpadLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.stubs.StubTree;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class SpadFile extends PsiFileBase {
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

    @NotNull
    @Override
    public StubTree calcStubTree() {
        try {
            return super.calcStubTree();
        }
        catch (RuntimeException e) {
            throw new AldorPsiException("Failed to calculate stubs for " + this.getVirtualFile(), e);
        }
    }

}
