package aldor.psi.impl;

import aldor.psi.AldorWhereBlock;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorWhereMixin extends StubBasedPsiElementBase<EmptyStub<AldorWhereBlock>> implements AldorWhereBlock {

    public AldorWhereMixin(@NotNull EmptyStub<AldorWhereBlock> stub, @NotNull IStubElementType<EmptyStub<AldorWhereBlock>, AldorWhereBlock> nodeType) {
        super(stub, nodeType);
    }

    public AldorWhereMixin(@NotNull ASTNode node) {
        super(node);
    }

    public AldorWhereMixin(EmptyStub<AldorWhereBlock> stub, IElementType nodeType, ASTNode node) {
        super(stub, nodeType, node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        //noinspection ObjectEquality
        if (lastParent == getWhereRhs()) {
            return true;
        }

        return getWhereRhs().processDeclarations(processor, state, lastParent, place);
    }
}
