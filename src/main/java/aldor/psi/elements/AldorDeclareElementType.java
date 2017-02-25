package aldor.psi.elements;

import aldor.language.AldorLanguage;
import aldor.psi.AldorDeclare;
import aldor.psi.stub.AldorDeclareStub;
import aldor.psi.stub.AldorDefineStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AldorDeclareElementType extends StubCodecElementType<AldorDeclareStub, AldorDeclare, AldorDeclareElementType> {
    public static final StubIndexKey<String, AldorDeclare> DECLARE_NAME_INDEX = StubIndexKey.createIndexKey("Aldor.Declare.Name");
    public static final StubIndexKey<String, AldorDeclare> DECLARE_TOP_INDEX = StubIndexKey.createIndexKey("Aldor.Declare.Top");

    public AldorDeclareElementType(String debugName, PsiStubCodec<AldorDeclareStub, AldorDeclare, AldorDeclareElementType> declareCodec) {
        super(debugName, AldorLanguage.INSTANCE, declareCodec);
    }

    @Override
    public void indexStub(@NotNull AldorDeclareStub stub, @NotNull IndexSink sink) {
        if (stub.isDeclareOfId()) {
            assert stub.declareIdName().isPresent();
            sink.occurrence(DECLARE_NAME_INDEX, stub.declareIdName().get());
        }
        if (stub.isDeclareOfId() && isSecondLevelDeclare(stub)) {
            assert stub.declareIdName().isPresent();
            sink.occurrence(DECLARE_TOP_INDEX, stub.declareIdName().get());
        }
    }

    private boolean isSecondLevelDeclare(AldorDeclareStub stub) {
        Optional<AldorDefineStub> definingForm = stub.definingForm();
        definingForm.filter(form -> form.defineInfo().level() == AldorDefineInfo.Level.TOP);

        return definingForm.isPresent();
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        // Ideally, we would see if this was part of a with, or a function.
        // in practice, that's a bit tricky, so...
        // (we can look at the parent element type if needed, maybe it's a with?)
        return true;
    }

    @Override
    AldorDeclareElementType toElType() {
        return this;
    }
}
