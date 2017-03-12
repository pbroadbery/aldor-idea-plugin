package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import aldor.lexer.AldorTokenTypes;
import aldor.psi.AldorLiteral;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Ids named in quotes.  Invented to annoy implementers.
 */
public class InfixedId extends AbstractId {
    private final SyntaxRepresentation<AldorLiteral> rep;

    public InfixedId(AldorLiteral id) {
        this.rep = SyntaxRepresentation.create((AldorTokenType) id.getFirstChild().getNode().getElementType(), id);
    }

    public InfixedId(SyntaxRepresentation<AldorLiteral> syntaxRepresentation) {
        this.rep = syntaxRepresentation;
    }


    @Override
    public String name() {
        return "LiteralId";
    }

    @Override
    @NotNull
    public String symbol() {
        if (rep.text().charAt(0) == '"') {
            //noinspection DynamicRegexReplaceableByCompiledPattern
            return rep.text().replace("\"", "");
        }
        return rep.text();
    }

    @Override
    public PsiElement psiElement() {
        return rep.element();
    }

    @Nullable
    public AldorLiteral aldorLiteral() {
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
        return syntaxVisitor.visitInfixedId(this);
    }

    @Override
    public String toString() {
        return symbol();
    }

    public static InfixedId createMissingId(String name) {
        return new InfixedId(new SyntaxRepresentation<AldorLiteral>() {
            @Nullable
            @Override
            public AldorLiteral element() {
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
}
