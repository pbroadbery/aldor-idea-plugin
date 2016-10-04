package aldor.psi.impl;

import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Comma;
import aldor.syntax.components.Declaration;
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
import java.util.Optional;

@SuppressWarnings({"AbstractClassExtendsConcreteClass", "AbstractClassWithOnlyOneDirectInheritor"})
public abstract class AldorDefineMixin extends ASTWrapperPsiElement {
    private static final Key<Optional<Syntax>> cachedLhsSyntax = new Key<>("LhsSyntax");

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
        Optional<Syntax> syntax = this.getUserData(cachedLhsSyntax);
        if (syntax == null) {
            Syntax calculatedSyntax = SyntaxPsiParser.parse(lhs);
            syntax = Optional.ofNullable(calculatedSyntax);
            this.putUserDataIfAbsent(cachedLhsSyntax, syntax);
        }

        if (syntax.isPresent()) {
            for (Syntax childScope: childScopesForDefineLhs(syntax.get())) {
                if (!childScope.psiElement().processDeclarations(processor, state, lastParent, place)) {
                    return false;
                }
            }
        }
        return true;
    }

    Iterable<Syntax> childScopesForDefineLhs(Syntax syntax) {
        if ((syntax == null) || !syntax.is(Declaration.class)) {
            return Collections.singleton(syntax);
        }
        Declaration decl = syntax.as(Declaration.class);
        assert decl != null;
        Collection<Syntax> scopes = new ArrayList<>();
        if (decl.lhs().is(Apply.class)) {
            Apply apply = decl.lhs().as(Apply.class);
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
        return scopes;
    }

}
