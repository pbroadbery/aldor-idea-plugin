package aldor.psi.impl;

import aldor.psi.AldorIterRepeatStatement;
import aldor.psi.AldorIterator;
import aldor.psi.AldorIterators;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import static aldor.AldorPsiUtils.Apply;
import static aldor.AldorPsiUtils.Comma;
import static aldor.AldorPsiUtils.Declaration;
import static aldor.AldorPsiUtils.Syntax;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorIterStatementMixin extends ASTWrapperPsiElement implements AldorIterRepeatStatement {
    private static final Key<Syntax> cachedLhsSyntax = new Key<>("LhsSyntax");

    protected AldorIterStatementMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {

        AldorIterators iters = this.getIterators();

        for (AldorIterator iter: iters.getIteratorList()) {
            if (lastParent == iter) {
                continue;
            }
            if (!iter.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
        }
        return true;
    }

    Iterable<Syntax> childScopesForDefineLhs(Syntax syntax) {
        if (!syntax.is(Declaration.class)) {
            return Collections.singleton(syntax);
        }
        Declaration decl = syntax.as(Declaration.class);
        // TODO: The var being defined here should not be included in this pass as it's
        //       really being added to the parent scope.
        assert decl != null;
        Collection<Syntax> scopes = new ArrayList<>();
        if (decl.lhs().is(Apply.class)) {
            Apply apply = decl.lhs().as(Apply.class);

            assert apply != null;
            for (Syntax child : apply.arguments()) {
                if (child.as(Comma.class) != null) {
                    for (Syntax commaElt: child.children()) {
                        scopes.add(commaElt);
                    }
                }
            }
        }
        return scopes;
    }


    boolean processLhsSyntax(Syntax syntax, Function<PsiElement, Boolean> fn) {
        System.out.println("Process lhs: " + syntax);
        fn.apply(syntax.psiElement());
        for (Syntax child : syntax.children()) {
            if (!processLhsSyntax(child, fn)) {
                return false;
            }
        }
        return true;
    }

}
