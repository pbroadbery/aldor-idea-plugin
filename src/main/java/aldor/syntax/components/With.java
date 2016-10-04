package aldor.syntax.components;

import com.intellij.psi.PsiElement;

/**
 * Created by pab on 04/10/16.
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
