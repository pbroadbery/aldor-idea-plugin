package aldor.psi.impl;

import aldor.psi.AldorDeclPart;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorId;
import aldor.psi.AldorPsiUtils;
import aldor.psi.stub.AldorDeclareStub;
import aldor.references.FileScopeWalker;
import aldor.references.ScopeContext;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.Id;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings({"AbstractClassWithOnlyOneDirectInheritor"})
public abstract class AldorDeclMixin extends AldorDeclareImpl implements AldorDeclPart {

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
        return this.getFirstChild();
    }

    @Override
    public PsiElement rhs() {
        return this.getType();
    }

    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("No rename on declarations");
    }

    @Override
    public PsiElement type() {
        Optional<AldorId> typeId = AldorPsiUtils.findUniqueIdentifier(rhs());
        if (typeId.isPresent() && (typeId.get().getReference() != null)) {
            PsiElement macro = typeId.get().getReference().resolveMacro();
            if (macro instanceof AldorDefine) {
                return ((AldorDefine) macro).rhs();
            }
        }
        return rhs(); // FIXME!
    }
}
