package aldor.references;

import aldor.psi.AldorId;
import aldor.psi.SpadAbbrevStubbing;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class AldorRefactoringSupportProvider extends RefactoringSupportProvider {

    @Override
    public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
        if (element instanceof AldorId) {
            return true;
        }
        if (element instanceof SpadAbbrevStubbing.SpadAbbrev) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
        return false;
    }

}
