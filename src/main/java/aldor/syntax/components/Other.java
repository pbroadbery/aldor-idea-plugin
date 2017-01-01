package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import aldor.syntax.Syntax;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Placeholder where we can't figure out what's going on.
 */
public class Other extends Syntax {
    private final PsiElement other;

    public Other(PsiElement other) {
        this.other = other;
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
    public Collection<Syntax> children() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public AldorTokenType tokenType() {
        return null;
    }

    @Override
    public String toString() {
        return "{?:" + name() + "}";
    }

}
