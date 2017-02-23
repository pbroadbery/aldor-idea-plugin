package aldor.psi.elements;

import aldor.psi.AldorDefine;
import aldor.psi.AldorPsiUtils;
import aldor.psi.impl.AldorDefineMixin;
import aldor.psi.stub.AldorDefineStub;
import aldor.psi.stub.impl.AldorDefineConcreteStub;
import aldor.util.StubCodec;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

import java.io.IOException;

class AldorDefineStubCodec implements PsiStubCodec<AldorDefineStub, AldorDefine> {
    private static final StubCodec<AldorDefineInfo> infoCodec = new AldorDefineInfoStubCodec();

    AldorDefineStubCodec() {

    }

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
    public AldorDefineStub decode(StubInputStream dataStream, IStubElementType<AldorDefineStub, AldorDefine> elementType, StubElement<?> parentStub) throws IOException {
        boolean nonNull = dataStream.readBoolean();
        String name = nonNull? dataStream.readUTFFast() : null;
        AldorDefineInfo defineInfo = infoCodec.decode(dataStream);
        return new AldorDefineConcreteStub(parentStub, elementType, name, defineInfo);
    }

    @Override
    public AldorDefine createPsi(IStubElementType<AldorDefineStub, AldorDefine> elementType, AldorDefineStub stub) {
        // Fixme: Should return the concrete version
        return new AldorDefineMixin(stub, elementType);
    }

    @Override
    public AldorDefineStub createStub(StubElement<?> parentStub, IStubElementType<AldorDefineStub, AldorDefine> elementType, AldorDefine aldorDefine) {
        String defineId = aldorDefine.defineIdentifier().map(PsiElement::getText).orElse(null);
        boolean isTopLevelDefine = AldorPsiUtils.isTopLevel(aldorDefine.getParent());
        AldorDefineInfo info = AldorDefineInfo.info(
                isTopLevelDefine ? AldorDefineInfo.Level.TOP: AldorDefineInfo.Level.INNER,
                AldorDefineInfo.Classification.OTHER);
        return new AldorDefineConcreteStub(parentStub, elementType, defineId, info);
    }

}
