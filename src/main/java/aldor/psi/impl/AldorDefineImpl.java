package aldor.psi.impl;

import aldor.psi.AldorDefine;
import aldor.psi.stub.AldorDefineStub;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.psi.stubs.IStubElementType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("StubBasedPsiElementBaseGetParent")
public class AldorDefineImpl extends StubBasedPsiElementBase<AldorDefineStub> implements AldorDefine {

    public AldorDefineImpl(@NotNull AldorDefineStub stub,
                           @NotNull IStubElementType<AldorDefineStub, AldorDefine> nodeType) {
        super(stub, nodeType);
    }

}
