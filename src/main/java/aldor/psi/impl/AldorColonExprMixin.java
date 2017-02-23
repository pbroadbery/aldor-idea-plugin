package aldor.psi.impl;

import aldor.psi.AldorColonExpr;
import aldor.psi.AldorDeclare;
import aldor.psi.stub.AldorDeclareStub;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorColonExprMixin extends AldorDeclareImpl implements AldorColonExpr {

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public AldorColonExprMixin(AldorDeclareStub stub, IStubElementType<AldorDeclareStub, AldorDeclare> elementType) {
        super(stub, elementType);
    }


    // grammar code gen forces this
    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public AldorColonExprMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        if (!processor.execute(this, state)) {
            return false;
        }
        PsiElement lhs = this.getFirstChild();
        //noinspection ObjectEquality
        if (lastParent != lhs) {
            Syntax lhsSyntax = SyntaxPsiParser.parse(lhs);
            if (lhsSyntax != null) {
                if (!lhsSyntax.psiElement().processDeclarations(processor, state, this, place)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public PsiElement lhs() {
        return this.getExprList().get(0);
    }
}
