package aldor.syntax;

import aldor.lexer.AldorTokenType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * Core representation of Syntax.. We don't use PSI as it's probably too heavyweight,
 * and somewhat subject to change.
 */
@SuppressWarnings("NewClassNamingConvention")
public abstract class Syntax {
    public abstract String name();

    public abstract PsiElement psiElement();

    public abstract Collection<Syntax> children();

    @NotNull
    public <T extends Syntax> T as(@NotNull Class<T> clzz) {
        if (clzz.isAssignableFrom(this.getClass())) {
            return clzz.cast(this);
        }
        throw new IllegalArgumentException("Expected a " + clzz.getName() + " got " + this.getClass() + " " + this);
    }

    public <T extends Syntax> Optional<T> maybeAs(Class<T> clzz) {
        return is(clzz) ? Optional.of(this.as(clzz)) : Optional.empty();
    }

    public <T extends Syntax> boolean is(@NotNull Class<T> clzz) {
        return clzz.isAssignableFrom(this.getClass());
    }

    public <T> T accept(SyntaxVisitor<T> syntaxVisitor) {
        return syntaxVisitor.visitSyntax(this);
    }

    @Nullable
    public abstract AldorTokenType tokenType();
}
