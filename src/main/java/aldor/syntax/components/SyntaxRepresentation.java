package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import aldor.psi.AldorIdentifier;
import aldor.psi.AldorLiteral;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public abstract class SyntaxRepresentation<T extends PsiElement> {

    public static <T extends PsiElement> SyntaxRepresentation<T> create(T element) {
        return new PsiSyntaxRepresentation<>(null, element);
    }

    public static <T extends PsiElement> SyntaxRepresentation<T> createMissing() {
        return new MissingSyntaxRepresentation<>();
    }

    @Nullable
    public abstract T element();

    @Nullable
    public abstract AldorTokenType tokenType();

    public abstract String text();

    public static SyntaxRepresentation<AldorIdentifier> create(AldorTokenType tokenType, AldorIdentifier id) {
        return new PsiSyntaxRepresentation<>(tokenType, id);
    }


    public static SyntaxRepresentation<AldorLiteral> create(AldorTokenType tokenType, AldorLiteral id) {
        return new PsiSyntaxRepresentation<>(tokenType, id);
    }

    private static class PsiSyntaxRepresentation<T extends PsiElement> extends SyntaxRepresentation<T> {
        private final T element;
        private final AldorTokenType tokenType;

        PsiSyntaxRepresentation(AldorTokenType tokenType, T element) {
            this.element = element;
            this.tokenType = tokenType;
        }

        @Override
        public T element() {
            return element;
        }

        @Nullable
        @Override
        public AldorTokenType tokenType() {
            return tokenType;
        }

        @Override
        public String text() {
            return element.getText();
        }
    }

    private static class MissingSyntaxRepresentation<T extends PsiElement> extends SyntaxRepresentation<T> {
        @Nullable
        @Override
        public T element() {
            return null;
        }

        @Override
        @Nullable
        public AldorTokenType tokenType() {
            return null;
        }

        @Override
        public String text() {
            return "Missing";
        }
    }
}
