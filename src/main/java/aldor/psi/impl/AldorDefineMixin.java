package aldor.psi.impl;

import aldor.AldorPsiUtils.AnyApply;
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
import java.util.List;
import java.util.function.Function;

import static aldor.AldorPsiUtils.Comma;
import static aldor.AldorPsiUtils.Declaration;
import static aldor.AldorPsiUtils.Syntax;
import static aldor.AldorPsiUtils.parse;

@SuppressWarnings({"AbstractClassExtendsConcreteClass", "AbstractClassWithOnlyOneDirectInheritor"})
public abstract class AldorDefineMixin extends ASTWrapperPsiElement implements PsiElement {
    private static final Key<Syntax> cachedLhsSyntax = new Key<>("LhsSyntax");

    protected AldorDefineMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        PsiElement lhs = getFirstChild();

        if (!processor.execute(this, state)) {
            return false;
        }

        if (lastParent == lhs) {
            return true;
        }
        Syntax syntax = this.getUserData(cachedLhsSyntax);
        if (syntax == null) {
            syntax = parse(lhs);
            this.putUserDataIfAbsent(cachedLhsSyntax, syntax);
        }

        for (Syntax childScope: childScopesForDefineLhs(syntax)) {
            System.out.println("Scope: " + childScope);
            if (!childScope.psiElement().processDeclarations(processor, state, lastParent, place)) {
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
        assert decl != null;
        Collection<Syntax> scopes = new ArrayList<>();
        if (decl.lhs().is(AnyApply.class)) {
            AnyApply<?> apply = decl.lhs().as(AnyApply.class);
            assert apply != null;
            List<Syntax> ll = apply.arguments();
            for (Syntax child : ll) {
                if (child.as(Comma.class) != null) {
                    for (Syntax commaElt: child.children()) {
                        scopes.add(commaElt);
                    }
                }
                else if (child.as(Declaration.class) != null){
                    scopes.add(child);
                }

            }
        }
        System.out.println("Scopes are: " + scopes);
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
