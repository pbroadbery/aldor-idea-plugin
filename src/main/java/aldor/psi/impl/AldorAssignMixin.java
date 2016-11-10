package aldor.psi.impl;

import aldor.psi.AldorAssign;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"AbstractClassExtendsConcreteClass", "AbstractClassWithOnlyOneDirectInheritor"})
public abstract class AldorAssignMixin extends ASTWrapperPsiElement implements AldorAssign {

    protected AldorAssignMixin(@NotNull ASTNode node) {
        super(node);
    }

}
