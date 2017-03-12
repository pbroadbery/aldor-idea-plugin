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

import static aldor.lexer.AldorTokenTypes.TK_Id;

/**
 * An identifier
 */
public class Id extends AbstractId {
    private final SyntaxRepresentation<AldorIdentifier> rep;

    public Id(AldorIdentifier id) {
        this.rep = SyntaxRepresentation.create((AldorTokenType) AldorTokenTypes.forText(id.getText()), id);
    }

    // For things like 'bracket'
    public static Id createImplicitId(String name) {
        return createMissingId(TK_Id, name);
    }

    // For when we don't have the PsiElement available.
    public static Id createMissingId(AldorTokenType tokenType, String name) {
        return new Id(new SyntaxRepresentation<AldorIdentifier>() {
            @Nullable
            @Override
            public AldorIdentifier element() {
                return null;
            }

            @Override
            public AldorTokenType tokenType() {
                return tokenType;
            }

            @Override
            public String text() {
                return name;
            }
        });
    }

    public static Syntax createImplicitId(PsiElement elt, String s) {
        return createMissingId((AldorTokenType) elt.getNode().getElementType(), s);
    }


    public Id(SyntaxRepresentation<AldorIdentifier> syntaxRepresentation) {
        this.rep = syntaxRepresentation;
    }

    @Override
    public String name() {
        return "Id";
    }

    @Override
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
