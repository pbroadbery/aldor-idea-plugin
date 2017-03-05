package aldor.psi.elements;

import aldor.language.AldorLanguage;
import aldor.psi.AldorDefine;
import aldor.psi.stub.AldorDefineStub;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class AldorDefineElementType extends StubCodecElementType<AldorDefineStub, AldorDefine, AldorDefineElementType> {
    private static final Logger LOG = Logger.getInstance(AldorDefineElementType.class);
    public static final StubIndexKey<String, AldorDefine> DEFINE_NAME_INDEX = StubIndexKey.createIndexKey("Aldor.Define.Name");
    public static final StubIndexKey<String, AldorDefine> DEFINE_TOPLEVEL_INDEX = StubIndexKey.createIndexKey("Aldor.Define.TopLevel");
    private final AldorDefine.DefinitionType type;

    public AldorDefineElementType(AldorDefine.DefinitionType type, PsiStubCodec<AldorDefineStub, AldorDefine, AldorDefineElementType> defineCodec) {
        //noinspection StringConcatenationMissingWhitespace
        super("Define" + type, AldorLanguage.INSTANCE, defineCodec);
        this.type = type;
    }

    @Override
    AldorDefineElementType toElType() {
        return this;
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "Aldor.Define."+ type;
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

    public AldorDefine.DefinitionType type() {
        return type;
    }
}
