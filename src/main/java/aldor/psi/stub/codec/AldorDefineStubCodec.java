package aldor.psi.stub.codec;

import aldor.psi.AldorDefine;
import aldor.psi.AldorPsiUtils;
import aldor.psi.elements.AldorDefineElementType;
import aldor.psi.elements.AldorDefineInfo;
import aldor.psi.elements.AldorStubFactory;
import aldor.psi.elements.PsiStubCodec;
import aldor.psi.stub.AldorDefineStub;
import aldor.psi.stub.impl.AldorDefineConcreteStub;
import aldor.util.StubCodec;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

import java.io.IOException;

public class AldorDefineStubCodec implements PsiStubCodec<AldorDefineStub, AldorDefine, AldorDefineElementType> {
    private static final StubCodec<AldorDefineInfo> infoCodec = new AldorDefineInfoStubCodec();
    private final AldorStubFactory.PsiElementFactory<AldorDefineStub, AldorDefine> elementFactory;

    public AldorDefineStubCodec(AldorStubFactory.PsiElementFactory<AldorDefineStub, AldorDefine> psiElementFactory) {
        this.elementFactory = psiElementFactory;
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
    public AldorDefineStub decode(StubInputStream dataStream, AldorDefineElementType elementType, StubElement<?> parentStub) throws IOException {
        boolean nonNull = dataStream.readBoolean();
        String name = nonNull? dataStream.readUTFFast() : null;
        AldorDefineInfo defineInfo = infoCodec.decode(dataStream);
        return new AldorDefineConcreteStub(parentStub, elementType, name, defineInfo);
    }

    @Override
    public AldorDefine createPsi(AldorDefineElementType elementType, AldorDefineStub stub) {
        return elementFactory.invoke(stub, elementType);
    }

    @Override
    public AldorDefineStub createStub(StubElement<?> parentStub, AldorDefineElementType elementType, AldorDefine aldorDefine) {
        String defineId = aldorDefine.defineIdentifier().map(PsiElement::getText).orElse(null);
        boolean isTopLevelDefine = AldorPsiUtils.isTopLevel(aldorDefine.getParent());
        AldorDefine.DefinitionType type = elementType.type();
        AldorDefineInfo info = AldorDefineInfo.info(
                isTopLevelDefine ? AldorDefineInfo.Level.TOP: AldorDefineInfo.Level.INNER,
                (type == AldorDefine.DefinitionType.MACRO) ? AldorDefineInfo.Classification.MACRO : AldorDefineInfo.Classification.OTHER);

        return new AldorDefineConcreteStub(parentStub, elementType, defineId, info);
    }

}
