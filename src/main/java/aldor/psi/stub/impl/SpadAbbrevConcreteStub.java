package aldor.psi.stub.impl;

import aldor.psi.SpadAbbrev;
import aldor.psi.stub.AbbrevInfo;
import aldor.psi.stub.SpadAbbrevStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

public class SpadAbbrevConcreteStub extends StubBase<SpadAbbrev> implements SpadAbbrevStub {
    private final AbbrevInfo info;

    public SpadAbbrevConcreteStub(StubElement<?> parent,
                                  IStubElementType<SpadAbbrevStub, SpadAbbrev> elementType,
                                  AbbrevInfo info) {
        super(parent, elementType);
        this.info = info;
    }

    @Override
    public AbbrevInfo info() {
        return info;
    }

}
