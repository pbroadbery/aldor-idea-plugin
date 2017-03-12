package aldor.psi.elements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public interface PsiElementCreator {
    PsiElement createElement(ASTNode node);
}
