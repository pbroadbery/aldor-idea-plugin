package aldor.syntax.components;

import aldor.lexer.AldorTokenType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import static aldor.lexer.AldorTokenTypes.forText;

public abstract class SyntaxRepresentation<T extends PsiElement> {

    public static <T extends PsiElement> SyntaxRepresentation<T> create(T element) {
        return new PsiSyntaxRepresentation<>(element);
    }

    public static <T extends PsiElement> SyntaxRepresentation<T> createMissing() {
        return new MissingSyntaxRepresentation<>();
    }

    @Nullable
    public abstract T element();

    public abstract AldorTokenType tokenType();

    public abstract String text();

    private static class PsiSyntaxRepresentation<T extends PsiElement> extends SyntaxRepresentation<T> {
        private final T element;

        PsiSyntaxRepresentation(T element) {
            this.element = element;
        }

        @Override
        public T element() {
            return element;
        }

        @Override
        public AldorTokenType tokenType() {
            return (AldorTokenType) forText(element.getText());
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
