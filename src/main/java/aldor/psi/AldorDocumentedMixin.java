package aldor.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * Something with documentation.
 */
@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorDocumentedMixin extends ASTWrapperPsiElement {

    protected AldorDocumentedMixin(@NotNull ASTNode node) {
        super(node);
    }
}
