package aldor.syntax.components;

import com.intellij.psi.PsiElement;

/**
 * Created by pab on 04/10/16.
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
