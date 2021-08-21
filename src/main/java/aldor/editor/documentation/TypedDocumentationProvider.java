package aldor.editor.documentation;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

class TypedDocumentationProvider<T extends PsiElement> {

    @Nullable
    public String generateDoc(T element, @Nullable PsiElement originalElement) {
        return null;
    }

    @Nullable
    public String getQuickNavigateInfo(T element, PsiElement originalElement) {
        return null;
    }
}
