package aldor.psi.elements;

import aldor.psi.SpadAbbrev;
import aldor.psi.SpadAbbrev.Classifier;
import aldor.psi.impl.SpadAbbrevMixin;
import aldor.psi.stub.AbbrevInfo;
import aldor.psi.stub.SpadAbbrevStub;
import aldor.psi.stub.impl.SpadAbbrevConcreteStub;
import aldor.util.StubCodec;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SpadAbbrevStubCodec implements PsiStubCodec<SpadAbbrevStub, SpadAbbrev> {
    static final StubCodec<AbbrevInfo> infoCodec = new AbbrevInfoCodec();

    SpadAbbrevStubCodec() {

    }

    @Override
    public void encode(SpadAbbrevStub stub, StubOutputStream dataStream) throws IOException {
        infoCodec.encode(dataStream, stub.info());
    }

    @Override
    public SpadAbbrevStub decode(StubInputStream dataStream, IStubElementType<SpadAbbrevStub, SpadAbbrev> eltType, StubElement<?> parentStub) throws IOException {
        AbbrevInfo info = infoCodec.decode(dataStream);
        return new SpadAbbrevConcreteStub(parentStub, eltType, info);
    }

    @Override
    public SpadAbbrev createPsi(IStubElementType<SpadAbbrevStub, SpadAbbrev> eltType, SpadAbbrevStub stub) {
        return new SpadAbbrevMixin(stub, eltType);
    }

    @Override
    public SpadAbbrevStub createStub(StubElement<?> parentStub, IStubElementType<SpadAbbrevStub, SpadAbbrev> eltType, SpadAbbrev spadAbbrev) {
        AbbrevInfo abbrevInfo = spadAbbrev.abbrevInfo();
        return new SpadAbbrevConcreteStub(parentStub, eltType, abbrevInfo);
    }

    private static class AbbrevInfoCodec implements StubCodec<AbbrevInfo> {
        @NotNull
        @Override
        public Class<AbbrevInfo> clzz() {
            return AbbrevInfo.class;
        }

        @Override
        public void encode(StubOutputStream stream, AbbrevInfo abbrevInfo) throws IOException {
            stream.writeInt(abbrevInfo.kind().ordinal());
            stream.writeName(abbrevInfo.name());
            stream.writeName(abbrevInfo.abbrev());
            stream.writeInt(abbrevInfo.nameIndex());
        }

        @Override
        public AbbrevInfo decode(StubInputStream stream) throws IOException {
            Classifier kind = SpadAbbrev.Classifier.values()[stream.readInt()];
            StringRef abbrev = stream.readName();
            StringRef name = stream.readName();
            int nameIndex = stream.readInt();
            if (abbrev == null) {
                throw new IOException("Failed to read abbrev");
            }
            if (name == null) {
                throw new IOException("Failed to read name");
            }
            return new AbbrevInfo(kind, abbrev.getString(), name.getString(), nameIndex);
        }
    }
}
