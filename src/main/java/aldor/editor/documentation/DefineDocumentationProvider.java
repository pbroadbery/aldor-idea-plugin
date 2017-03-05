package aldor.editor.documentation;

import aldor.psi.AldorDefine;
import com.intellij.psi.PsiElement;

class DefineDocumentationProvider extends TypedDocumentationProvider<AldorDefine> {

    @Override
    public String generateDoc(AldorDefine o, PsiElement originalElement) {
        return "I'm a definition";
    }

}
