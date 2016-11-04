package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import aldor.psi.AldorIdentifier;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * CAn identifier
 */
public class Id extends Syntax {
    private final SyntaxRepresentation<AldorIdentifier> rep;

    public Id(AldorIdentifier id) {
        this.rep = SyntaxRepresentation.create(id);
    }

    public Id(SxSyntaxRepresentation<AldorIdentifier> sxSyntaxRepresentation) {
        this.rep = sxSyntaxRepresentation;
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

    @Override
    public Iterable<Syntax> children() {
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
