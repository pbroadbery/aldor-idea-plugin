package aldor.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CollectingAldorVisitor<T> extends AldorVisitor {
    private final List<T> returnValue = new ArrayList<>();

    public CollectingAldorVisitor() {
    }

    void doAcceptChildren(PsiElement o) {
        for (PsiElement child : o.getChildren()) {
            child.accept(this);
        }
    }

    protected void add(@NotNull T t) {
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
