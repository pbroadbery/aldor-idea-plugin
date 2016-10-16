package aldor.syntax.components;

import com.intellij.psi.PsiElement;

/**
 * Add syntax
 */
public class Add extends Other {

    public Add(PsiElement other) {
        super(other);
    }

    @Override
    public String name() {
        return "Add";
    }

}
