package aldor.psi.impl;

import aldor.psi.AldorVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CollectingAldorVisitor<T> extends AldorVisitor {
    @Nullable
    private final List<T> returnValue = new ArrayList<>();

    public CollectingAldorVisitor() {
    }

    void doAcceptChildren(PsiElement o) {
        for (PsiElement child : o.getChildren()) {
            child.accept(this);
            //noinspection VariableNotUsedInsideIf
            if (returnValue != null) {
                break;
            }
        }
    }

    protected void add(T t) {
        this.returnValue.add(t);
    }

    public List<T> returnValue() {
        return returnValue;
    }

    public List<T> apply(PsiElement element) {
        element.accept(this);
        return returnValue();
    }
}
