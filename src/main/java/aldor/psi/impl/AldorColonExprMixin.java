package aldor.psi.impl;

import aldor.psi.AldorColonExpr;
import aldor.psi.AldorDeclare;
import aldor.psi.stub.AldorDeclareStub;
import aldor.references.FileScopeWalker;
import aldor.references.ScopeContext;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.Id;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorColonExprMixin extends StubBasedPsiElementBase<AldorDeclareStub> implements AldorColonExpr, AldorDeclare {

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
        if (state.get(FileScopeWalker.scopeContextKey) == ScopeContext.DeclBlock) {
            return true;
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
    public String getName() {
        return SyntaxUtils.leadingId(SyntaxPsiParser.parse(lhs()))
                .maybeAs(Id.class)
                .map(Id::symbol).orElse(null);
    }

    @Override
    public PsiElement lhs() {
        return this.getExprList().get(0);
    }

    @Override
    public PsiElement rhs() {
        return this.getExprList().get(1);
    }

    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("no rename for definitions (yet)");
    }
}
