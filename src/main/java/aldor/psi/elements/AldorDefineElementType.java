package aldor.psi.elements;

import aldor.language.AldorLanguage;
import aldor.psi.AldorDefine;
import aldor.psi.impl.AldorDefineImpl;
import aldor.psi.stub.AldorDefineConcreteStub;
import aldor.psi.stub.AldorDefineStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AldorDefineElementType extends IStubElementType<AldorDefineStub, AldorDefine> {

    public AldorDefineElementType() {
        super("Define", AldorLanguage.INSTANCE);
    }

    @Override
    public AldorDefine createPsi(@NotNull AldorDefineStub stub) {
        return new AldorDefineImpl(stub, this);
    }

    @Override
    public AldorDefineStub createStub(@NotNull AldorDefine psi, @SuppressWarnings("rawtypes") StubElement parentStub) {
        return new AldorDefineConcreteStub(parentStub);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "Aldor.Define";
    }

    @Override
    public void serialize(@NotNull AldorDefineStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        //dataStream.writeUTFFast(stub.name());
    }

    @NotNull
    @Override
    public AldorDefineStub deserialize(@NotNull StubInputStream dataStream, @SuppressWarnings("rawtypes") StubElement parentStub) throws IOException {
        //return new AldorDefineConcreteStub(parentStub, dataStream.readUTFFast(), "nope");
        return new AldorDefineConcreteStub(parentStub);
    }

    @Override
    public void indexStub(@NotNull AldorDefineStub stub, @NotNull IndexSink sink) {
        //sink.occurrence(EXPR_DEFINE_KEY, stub.name());
    }

}
