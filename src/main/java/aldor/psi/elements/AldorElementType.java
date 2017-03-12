package aldor.psi.elements;

import aldor.language.AldorLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;

public class AldorElementType extends IElementType implements PsiElementCreator {
    public AldorElementType(@NonNls String debugName) {
        super(debugName, AldorLanguage.INSTANCE);
    }

    @Override
    @SuppressWarnings({"HardCodedStringLiteral"})
    public String toString() {
        return "{ET: " + super.toString() + "}";
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        return AldorTypes.Factory.createElement(node);
    }
}
