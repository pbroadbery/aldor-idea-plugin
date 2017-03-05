package aldor.editor.documentation;

import com.intellij.psi.PsiElement;

class TypeAndProvider<T extends PsiElement> {

    private final Class<T> clzz;
    private final TypedDocumentationProvider<T> instance;

    TypeAndProvider(Class<T> clzz, TypedDocumentationProvider<T> docProvider) {
        this.clzz = clzz;
        this.instance = docProvider;
    }

    public Class<T> clzz() {
        return clzz;
    }

    public TypedDocumentationProvider<T> instance() {
        return instance;
    }
}
