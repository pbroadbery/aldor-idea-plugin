package aldor.editor.documentation;

import aldor.psi.AldorDeclare;
import com.intellij.psi.PsiElement;

class DeclareDocumentationProvider extends TypedDocumentationProvider<AldorDeclare> {
    @Override
    public String generateDoc(AldorDeclare o, PsiElement originalElement) {
        return "I'm a declaration";
    }


}
