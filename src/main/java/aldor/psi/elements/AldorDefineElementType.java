package aldor.psi.elements;

import aldor.language.AldorLanguage;
import aldor.psi.AldorDefine;
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

public class AldorDefineElementType extends IStubElementType<AldorDefine.AldorDefineStub, AldorDefine> {
    private static final Logger LOG = Logger.getInstance(AldorDefineElementType.class);
    public static final StubIndexKey<String, AldorDefine> DEFINE_NAME_INDEX = StubIndexKey.createIndexKey("Aldor.Define.Name");
    public static final StubIndexKey<String, AldorDefine> DEFINE_TOPLEVEL_INDEX = StubIndexKey.createIndexKey("Aldor.Define.TopLevel");
    private static final IndexCodec<AldorDefineInfo> infoCodec = new AldorDefineInfoIndexCodec();

    private final AldorStubFactory stubFactory;

    public AldorDefineElementType(AldorStubFactory stubFactory) {
        super("Define", AldorLanguage.INSTANCE);
        this.stubFactory = stubFactory;
    }

    @Override
    public AldorDefine createPsi(@NotNull AldorDefine.AldorDefineStub stub) {
        return stub.createPsi(this);
    }

    @Override
    public AldorDefine.AldorDefineStub createStub(@NotNull AldorDefine psi, @SuppressWarnings("rawtypes") StubElement parentStub) {
        return psi.createStub(this, parentStub);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "Aldor.Define";
    }

    @Override
    public void serialize(@NotNull AldorDefine.AldorDefineStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        // TODO: stub factory becomes a codec store, this becomes codecStore.encode(AldorDefine.class, stub, dataStream)
        String defineId = stub.defineId();
        dataStream.writeBoolean(defineId != null);
        if (defineId != null) {
            dataStream.writeUTFFast(defineId);
        }
        infoCodec.encode(dataStream, stub.defineInfo());
    }

    @NotNull
    @Override
    public AldorDefine.AldorDefineStub deserialize(@NotNull StubInputStream dataStream, @SuppressWarnings("rawtypes") StubElement parentStub) throws IOException {
        return stubFactory.createStub(AldorDefine.class, this, dataStream, parentStub);
    }

    @Override
    public void indexStub(@NotNull AldorDefine.AldorDefineStub stub, @NotNull IndexSink sink) {
        if (stub.defineId() != null) {
            sink.occurrence(DEFINE_NAME_INDEX, stub.defineId());
            if (stub.defineInfo().level() == AldorDefineInfo.Level.TOP) {
                sink.occurrence(DEFINE_TOPLEVEL_INDEX, stub.defineId());
            }
        }
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return true;
    }
}
