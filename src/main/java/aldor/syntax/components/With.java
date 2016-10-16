package aldor.syntax.components;

import com.intellij.psi.PsiElement;

/**
 * "With" syntax node.
 */
public class With extends Other {

    public With(PsiElement other) {
        super(other);
    }

    @Override
    public String name() {
        return "With";
    }
}
