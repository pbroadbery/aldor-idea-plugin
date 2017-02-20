package aldor.psi.elements;

import aldor.language.AldorLanguage;
import aldor.psi.AldorDeclareStubbing.AldorDeclareStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static aldor.psi.AldorDeclareStubbing.AldorDeclare;

public class AldorDeclareElementType extends IStubElementType<AldorDeclareStub, AldorDeclare> {
    public static final StubIndexKey<String, AldorDeclare> DECLARE_NAME_INDEX = StubIndexKey.createIndexKey("Aldor.Declare.Name");
    private final PsiStubCodec<AldorDeclareStub, AldorDeclare> declareCodec;
    private final String externalId;

    public AldorDeclareElementType(AldorStubFactory stubFactory, String debugName, PsiStubCodec<AldorDeclareStub, AldorDeclare> declareCodec) {
        super(debugName, AldorLanguage.INSTANCE);
        this.declareCodec = declareCodec;
        this.externalId = debugName;
    }

    @Override
    public AldorDeclare createPsi(@NotNull AldorDeclareStub stub) {
        return declareCodec.createPsi(this, stub);
    }

    @NotNull
    @Override
    public AldorDeclareStub createStub(@NotNull AldorDeclare psi, @SuppressWarnings("rawtypes") StubElement parentStub) {
        return declareCodec.createStub(parentStub, this, psi);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return externalId;
    }

    @Override
    public void serialize(@NotNull AldorDeclareStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        declareCodec.encode(stub, dataStream);
    }

    @NotNull
    @Override
    public AldorDeclareStub deserialize(@NotNull StubInputStream dataStream, @SuppressWarnings("rawtypes") StubElement parentStub) throws IOException {
        return declareCodec.decode(dataStream, this, parentStub);
    }

    @Override
    public void indexStub(@NotNull AldorDeclareStub stub, @NotNull IndexSink sink) {
        if (stub.isDeclareOfId()) {
            assert stub.declareIdName().isPresent();
            sink.occurrence(DECLARE_NAME_INDEX, stub.declareIdName().get());
        }
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        // Ideally, we would see if this was part of a with, or a function.
        // in practice, that's a bit tricky, so
        return true;
    }

}
