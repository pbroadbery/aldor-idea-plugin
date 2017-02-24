package aldor.psi.elements;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class StubCodecElementType<CStub extends StubElement<CPsi>, CPsi extends PsiElement> extends IStubElementType<CStub, CPsi> {

    private final String externalId;
    private final PsiStubCodec<CStub, CPsi> codec;

    protected StubCodecElementType(@NotNull @NonNls String debugName, @NotNull Language language, PsiStubCodec<CStub, CPsi> codec) {
        super(debugName, language);
        this.externalId = language.getID() + "." + debugName;
        this.codec = codec;
    }

    @Override
    public final CPsi createPsi(@NotNull CStub stub) {
        return codec.createPsi(this, stub);
    }

    @NotNull
    @Override
    public final CStub createStub(@NotNull CPsi psi, @SuppressWarnings("rawtypes") StubElement parentStub) {
        return codec.createStub(parentStub, this, psi);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return externalId;
    }

    @Override
    public final void serialize(@NotNull CStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        codec.encode(stub, dataStream);
    }

    @NotNull
    @Override
    public final CStub deserialize(@NotNull StubInputStream dataStream, @SuppressWarnings("rawtypes") StubElement parentStub) throws IOException {
        return codec.decode(dataStream, this, parentStub);
    }

    public static class NoIndexElementType<IStub extends StubElement<IPsi>, IPsi extends PsiElement> extends StubCodecElementType<IStub, IPsi> {

        protected NoIndexElementType(@NotNull @NonNls String debugName, @NotNull Language language, PsiStubCodec<IStub, IPsi> codec) {
            super(debugName, language, codec);
        }

        @Override
        public final void indexStub(@NotNull IStub stub, @NotNull IndexSink sink) {
        }
    }

}
