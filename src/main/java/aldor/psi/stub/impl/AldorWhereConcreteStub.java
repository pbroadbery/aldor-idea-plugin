package aldor.psi.stub.impl;

import aldor.psi.AldorWhereBlock;
import aldor.psi.stub.AldorDefineStub;
import aldor.psi.stub.AldorWhereStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

import java.util.List;

public class AldorWhereConcreteStub extends StubBase<AldorWhereBlock> implements AldorWhereStub {

    protected AldorWhereConcreteStub(StubElement<?> parent, IStubElementType<AldorWhereStub, AldorWhereBlock> elementType) {
        super(parent, elementType);
    }

    @Override
    public List<AldorDefineStub> definitions() {
        return null;
    }
}
