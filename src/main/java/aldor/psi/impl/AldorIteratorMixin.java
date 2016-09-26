package aldor.psi.impl;

import aldor.psi.AldorForLhs;
import aldor.psi.AldorId;
import aldor.psi.AldorIterator;
import aldor.psi.AldorRecursiveVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings({"AbstractClassExtendsConcreteClass", "AbstractClassWithOnlyOneDirectInheritor"})
public abstract class AldorIteratorMixin extends ASTWrapperPsiElement implements AldorIterator {

    protected AldorIteratorMixin(@NotNull ASTNode node) {
        super(node);
    }


    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        if (this.getForLhs() == null) {
            return true;
        }

        for (PsiElement elt: findForVariables(this.getForLhs())) {
            if (elt == lastParent) {
                continue;
            }
            if (!processor.execute(elt, state)) {
                return false;
            }
        }
        return true;
    }

    private Iterable<PsiElement> findForVariables(AldorForLhs forLhs) {
        if (forLhs.getKWFree() != null) {
            return Collections.emptyList();
        }
        final Collection<PsiElement> vars = new ArrayList<>();
        forLhs.getInfixed().acceptChildren(new AldorRecursiveVisitor() {
            // This is a bit wrong, as it might be possible to put decls and similar in for loops..

            @Override
            public void visitId(@NotNull AldorId o) {
                vars.add(o);
            }
        });
        return vars;
    }
}
