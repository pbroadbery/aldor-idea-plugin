package aldor.psi.impl;

import aldor.psi.AldorAssign;
import aldor.psi.AldorRecursiveVisitor;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.components.SyntaxUtils;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

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

        //noinspection ObjectEquality
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
            for (Syntax childScope: SyntaxUtils.childScopesForDefineLhs(syntax.get())) {
                if (!childScope.psiElement().processDeclarations(processor, state, lastParent, place)) {
                    return false;
                }
            }
        }

        PsiElement rhs = this.getLastChild();

        rhs.accept(new AldorRecursiveVisitor() {
            @Override
            public void visitElement(PsiElement o) {
                if (o == lastParent)
                    return;
                o.acceptChildren(this);
            }

            @Override
            public void visitAssign(AldorAssign assign) {

            }
        });
        return true;
    }
}
