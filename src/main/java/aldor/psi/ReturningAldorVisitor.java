package aldor.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class ReturningAldorVisitor<T> extends AldorVisitor {
    @Nullable
    private T returnValue;

    public ReturningAldorVisitor() {
        this.returnValue = null;
    }

    protected void doAcceptChildren(PsiElement o) {
        for (PsiElement child : o.getChildren()) {
            child.accept(this);
            //noinspection VariableNotUsedInsideIf
            if (returnValue != null) {
                break;
            }
        }
    }

    protected void returnValue(T t) {
        this.returnValue = t;
    }

    public T returnValue() {
        return returnValue;
    }

    public T apply(PsiElement element) {
        element.accept(this);
        return returnValue();
    }
}
