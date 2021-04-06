package aldor.psi.impl;

import aldor.psi.AldorWith;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.IStubElementType;
import org.jetbrains.annotations.NotNull;

public class AldorWithMixin extends StubBasedPsiElementBase<EmptyStub<AldorWith>> implements AldorWith {
    public AldorWithMixin(@NotNull ASTNode node) {
        super(node);
    }

    public AldorWithMixin(EmptyStub<AldorWith> stub, @SuppressWarnings("rawtypes") IStubElementType type) {
        super(stub, type);
    }
}