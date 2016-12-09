package aldor.psi.elements;

import aldor.psi.SpadAbbrevStubbing.AbbrevInfo;
import aldor.psi.SpadAbbrevStubbing.Classifier;
import aldor.psi.impl.SpadAbbrevStubbingImpl;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

import java.io.IOException;

import static aldor.psi.SpadAbbrevStubbing.SpadAbbrev;
import static aldor.psi.SpadAbbrevStubbing.SpadAbbrevStub;

public class SpadAbbrevStubCodec implements PsiStubCodec<SpadAbbrevStub, SpadAbbrev> {
    static final IndexCodec<AbbrevInfo> infoCodec = new AbbrevInfoCodec();

    @Override
    public void encode(SpadAbbrevStub stub, StubOutputStream dataStream) throws IOException {
        infoCodec.encode(dataStream, stub.info());
    }

    @Override
    public SpadAbbrevStub decode(IStubElementType<SpadAbbrevStub, SpadAbbrev> eltType,
                                 StubInputStream dataStream, StubElement<?> parentStub) throws IOException {
        AbbrevInfo info = infoCodec.decode(dataStream);
        return new SpadAbbrevStubbingImpl.SpadAbbrevConcreteStub(parentStub, eltType, info);
    }

    private static class AbbrevInfoCodec implements IndexCodec<AbbrevInfo> {
        @Override
        public void encode(StubOutputStream stream, AbbrevInfo abbrevInfo) throws IOException {
            stream.writeInt(abbrevInfo.kind().ordinal());
            stream.writeName(abbrevInfo.name());
            stream.writeName(abbrevInfo.abbrev());
            stream.writeInt(abbrevInfo.nameIndex());
        }

        @Override
        public AbbrevInfo decode(StubInputStream stream) throws IOException {
            Classifier kind = Classifier.values()[stream.readInt()];
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
