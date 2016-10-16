package aldor.syntax.components;

import aldor.syntax.Syntax;
import aldor.util.SExpression;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * Placeholder where we can't figure out what's going on.
 */
public class OtherSx extends Syntax {
    @Nullable
    private final PsiElement other;
    private final SExpression sx;

    public OtherSx(SExpression sx) {
        this.other = null;
        this.sx = sx;
    }

    @Override
    public String name() {
        return "Other";
    }

    @Override
    public PsiElement psiElement() {
        return other;
    }

    @Override
    @Nullable
    public Iterable<Syntax> children() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "(" + name() + ": " + sx + ")";
    }

}
