package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import aldor.lexer.AldorTokenTypes;
import aldor.psi.AldorIdentifier;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * CAn identifier
 */
public class Id extends Syntax {
    private final SyntaxRepresentation<AldorIdentifier> rep;

    public Id(AldorIdentifier id) {
        this.rep = SyntaxRepresentation.create(id);
    }

    // For things like 'bracket'
    public static Id createImplicitId(String name) {
        return createMissingId(name);
    }

    // For when we don't have the PsiElement available.
    public static Id createMissingId(String name) {
        return new Id(new SyntaxRepresentation<AldorIdentifier>() {
            @Nullable
            @Override
            public AldorIdentifier element() {
                return null;
            }

            @Override
            public AldorTokenType tokenType() {
                return AldorTokenTypes.TK_Id;
            }

            @Override
            public String text() {
                return name;
            }
        });
    }

    public Id(SyntaxRepresentation<AldorIdentifier> syntaxRepresentation) {
        this.rep = syntaxRepresentation;
    }

    @Override
    public String name() {
        return "Id";
    }

    @NotNull
    public String symbol() {
        return rep.text();
    }

    @Override
    public PsiElement psiElement() {
        return rep.element();
    }

    @Nullable
    public AldorIdentifier aldorIdentifier() {
        return rep.element();
    }

    @Override
    public Collection<Syntax> children() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public AldorTokenType tokenType() {
        return rep.tokenType();
    }

    @Override
    public <T> T accept(SyntaxVisitor<T> syntaxVisitor) {
        return syntaxVisitor.visitId(this);
    }

    @Override
    public String toString() {
        return symbol();
    }
}
