package aldor.psi.stub;

import aldor.psi.AldorDefine;
import aldor.psi.elements.AldorElementTypeFactory;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

public class AldorDefineConcreteStub extends StubBase<AldorDefine> implements AldorDefineStub {

    @SuppressWarnings("rawtypes")
    public AldorDefineConcreteStub(StubElement parent) {
        super(parent, AldorElementTypeFactory.DEFINE_ELEMENT_TYPE);
    }
}
