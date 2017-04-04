package aldor.file;

import aldor.language.AldorLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.stubs.StubTree;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class AldorFile extends PsiFileBase {
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


    @NotNull
    @Override
    public StubTree calcStubTree() {
        try {
            return super.calcStubTree();
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw new AldorPsiException("Failed to calculate stubs for " + this.getVirtualFile(), e);
        }
    }
}
