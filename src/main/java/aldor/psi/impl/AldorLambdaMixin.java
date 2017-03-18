package aldor.psi.impl;

import aldor.psi.AldorLambda;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AldorLambdaMixin extends ASTWrapperPsiElement implements AldorLambda {
    private static final Key<Optional<Syntax>> cachedLhsSyntax = new Key<>("LhsSyntax");

    public AldorLambdaMixin(@NotNull ASTNode node) {
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
        Optional<Syntax> syntax = syntax();

        if (syntax.isPresent()) {
            for (Syntax childScope: SyntaxUtils.childScopesForLambdaLhs(syntax.get())) {
                if (!childScope.psiElement().processDeclarations(processor, state, lastParent, place)) {
                    return false;
                }
            }
        }

        return true;
    }

    @NotNull
    private Optional<Syntax> syntax() {
        PsiElement lhs = getFirstChild();
        Optional<Syntax> syntax = this.getUserData(cachedLhsSyntax);
        if (syntax == null) {
            Syntax calculatedSyntax = SyntaxPsiParser.parse(lhs);
            syntax = Optional.ofNullable(calculatedSyntax);
            this.putUserDataIfAbsent(cachedLhsSyntax, syntax);
        }
        return syntax;
    }


}
