package aldor.psi.elements;

import aldor.psi.AldorDefine;
import aldor.psi.impl.AldorDefineMixin;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;

import java.io.IOException;

/**
 *
 */
public class AldorStubFactoryImpl implements AldorStubFactory {
    private static final Logger LOG = Logger.getInstance(AldorStubFactoryImpl.class);
    private static final IndexCodec<AldorDefineInfo> infoCodec = new AldorDefineInfoIndexCodec();

    @Override
    public <PsiElt extends PsiElement, StubElt extends StubElement<PsiElt>> StubElt createStub(Class<PsiElt> clss,
                                                                                               IStubElementType<StubElt, PsiElt> eltType,
                                                                                               StubInputStream dataStream,
                                                                                               StubElement<?> parentStub) throws IOException {
        boolean nonNull = dataStream.readBoolean();
        String name = nonNull? dataStream.readUTFFast() : null;
        AldorDefineInfo defineInfo = infoCodec.decode(dataStream);
        //noinspection unchecked
        IStubElementType<AldorDefine.AldorDefineStub, AldorDefine> elementType = (IStubElementType<AldorDefine.AldorDefineStub, AldorDefine>) eltType;
        AldorDefineMixin.AldorDefineConcreteStub stub = new AldorDefineMixin.AldorDefineConcreteStub(parentStub, elementType, name, defineInfo);
        //noinspection unchecked
        return (StubElt) stub;

    }

    @Override
    public int getVersion() {
        return 2;
    }
}
