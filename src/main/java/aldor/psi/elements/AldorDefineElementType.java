package aldor.psi.elements;

import aldor.language.AldorLanguage;
import aldor.psi.AldorDefine;
import aldor.psi.stub.AldorDefineStub;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class AldorDefineElementType extends StubCodecElementType<AldorDefineStub, AldorDefine> {
    private static final Logger LOG = Logger.getInstance(AldorDefineElementType.class);
    public static final StubIndexKey<String, AldorDefine> DEFINE_NAME_INDEX = StubIndexKey.createIndexKey("Aldor.Define.Name");
    public static final StubIndexKey<String, AldorDefine> DEFINE_TOPLEVEL_INDEX = StubIndexKey.createIndexKey("Aldor.Define.TopLevel");

    public AldorDefineElementType(AldorStubFactory stubFactory) {
        super("Define", AldorLanguage.INSTANCE, stubFactory.defineCodec());
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
    public void indexStub(@NotNull AldorDefineStub stub, @NotNull IndexSink sink) {
        if (stub.defineId() != null) {
            sink.occurrence(DEFINE_NAME_INDEX, stub.defineId());
            if (stub.defineInfo().level() == AldorDefineInfo.Level.TOP) {
                sink.occurrence(DEFINE_TOPLEVEL_INDEX, stub.defineId());
            }
        }
    }

}
