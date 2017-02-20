package aldor.psi.impl;

import aldor.psi.AldorDeclPart;
import aldor.psi.AldorDeclareStubbing.AldorDeclare;
import aldor.psi.AldorDeclareStubbing.AldorDeclareStub;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"AbstractClassExtendsConcreteClass", "AbstractClassWithOnlyOneDirectInheritor"})
public abstract class AldorDeclMixin extends AldorDeclareStubbingImpl.AldorDeclareImpl implements AldorDeclPart {

    protected AldorDeclMixin(@NotNull ASTNode node) {
        super(node);
    }

    protected AldorDeclMixin(AldorDeclareStub stub, IStubElementType<AldorDeclareStub, AldorDeclare> elementType) {
        super(stub, elementType);
    }


    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        if (!processor.execute(this, state)) {
            return false;
        }
        PsiElement lhs = this.lhs();
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
        return getFirstChild();
    }
}
