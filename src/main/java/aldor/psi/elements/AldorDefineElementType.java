package aldor.psi.elements;

import aldor.language.AldorLanguage;
import aldor.psi.AldorDefineStubbing.AldorDefine;
import aldor.psi.AldorDefineStubbing.AldorDefineStub;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AldorDefineElementType extends IStubElementType<AldorDefineStub, AldorDefine> {
    private static final Logger LOG = Logger.getInstance(AldorDefineElementType.class);
    public static final StubIndexKey<String, AldorDefine> DEFINE_NAME_INDEX = StubIndexKey.createIndexKey("Aldor.Define.Name");
    public static final StubIndexKey<String, AldorDefine> DEFINE_TOPLEVEL_INDEX = StubIndexKey.createIndexKey("Aldor.Define.TopLevel");
    private final PsiStubCodec<AldorDefineStub, AldorDefine> defineCodec;

    public AldorDefineElementType(AldorStubFactory stubFactory) {
        super("Define", AldorLanguage.INSTANCE);
        defineCodec = stubFactory.defineCodec();
    }

    @Override
    public AldorDefine createPsi(@NotNull AldorDefineStub stub) {
        return defineCodec.createPsi(this, stub);
    }

    @NotNull
    @Override
    public AldorDefineStub createStub(@NotNull AldorDefine psi, @SuppressWarnings("rawtypes") StubElement parentStub) {
        return defineCodec.createStub(parentStub, this, psi);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "Aldor.Define";
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return true;
    }

    @Override
    public void serialize(@NotNull AldorDefineStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        defineCodec.encode(stub, dataStream);
    }

    @NotNull
    @Override
    public AldorDefineStub deserialize(@NotNull StubInputStream dataStream, @SuppressWarnings("rawtypes") StubElement parentStub) throws IOException {
        return defineCodec.decode(dataStream, this, parentStub);
    }

    @Override
    public void indexStub(@NotNull AldorDefineStub stub, @NotNull IndexSink sink) {
        if (stub.defineId() != null) {
            sink.occurrence(DEFINE_NAME_INDEX, stub.defineId());
            if (stub.defineInfo().level() == AldorDefineInfo.Level.TOP) {
                sink.occurrence(DEFINE_TOPLEVEL_INDEX, stub.defineId());
            }
        }
    }

}
