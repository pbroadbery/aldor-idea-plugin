package aldor.psi.elements;

import aldor.psi.AldorDefineStubbing.AldorDefine;
import aldor.psi.AldorDefineStubbing.AldorDefineStub;
import aldor.psi.impl.AldorDefineMixin;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

import java.io.IOException;

class AldorDefineStubCodec implements PsiStubCodec<AldorDefineStub, AldorDefine> {
    private static final IndexCodec<AldorDefineInfo> infoCodec = new AldorDefineInfoIndexCodec();

    @Override
    public void encode(AldorDefineStub stub, StubOutputStream dataStream) throws IOException {
        String defineId = stub.defineId();
        dataStream.writeBoolean(defineId != null);
        if (defineId != null) {
            dataStream.writeUTFFast(defineId);
        }
        infoCodec.encode(dataStream, stub.defineInfo());
    }

    @Override
    public AldorDefineStub decode(IStubElementType<AldorDefineStub, AldorDefine> elementType,
                                  StubInputStream dataStream, StubElement<?> parentStub) throws IOException {
        boolean nonNull = dataStream.readBoolean();
        String name = nonNull? dataStream.readUTFFast() : null;
        AldorDefineInfo defineInfo = infoCodec.decode(dataStream);
        return new AldorDefineMixin.AldorDefineConcreteStub(parentStub, elementType, name, defineInfo);
    }
}
