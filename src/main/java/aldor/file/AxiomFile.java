package aldor.file;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.stubs.StubTree;
import org.jetbrains.annotations.NotNull;

public abstract class AxiomFile extends PsiFileBase {
    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public AxiomFile(FileViewProvider viewProvider, Language language) {
        super(viewProvider, language);
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
