package aldor.psi.elements;

import aldor.psi.AldorDeclare;
import aldor.psi.stub.AldorDeclareStub;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class AldorDeclareElementType extends AldorStubElementType<AldorDeclareStub, AldorDeclare, AldorDeclareElementType> {
    public static final StubIndexKey<String, AldorDeclare> DECLARE_TOP_INDEX = StubIndexKey.createIndexKey("Aldor.Declare.Top");
    //public static final StubIndexKey<String, AldorDeclare> DECLARE_NAME_INDEX = StubIndexKey.createIndexKey("Aldor.Declare.Name"); //?? Overkill
    private static final Logger LOG = Logger.getInstance(AldorDeclareElementType.class);

    public AldorDeclareElementType(String debugName, PsiStubCodec<AldorDeclareStub, AldorDeclare, AldorDeclareElementType> declareCodec) {
        super(debugName, declareCodec);
    }

    @Override
    public void indexStub(@NotNull AldorDeclareStub stub, @NotNull IndexSink sink) {
        if (isCategoryDeclaration(stub)) {
            if (!stub.declareIdName().isPresent()) {
                LOG.warn("no declare id: " + stub.syntax());
            }
            else {
                sink.occurrence(DECLARE_TOP_INDEX, stub.declareIdName().get());
            }
        }
    }

    private boolean isCategoryDeclaration(AldorDeclareStub stub) {
        return stub.isCategoryDeclaration();
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
