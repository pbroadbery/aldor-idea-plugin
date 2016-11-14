package aldor.expression;

import aldor.expression.psi.ExpressionDefine;
import aldor.expression.psi.impl.ExpressionDefineStubImpl;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static aldor.expression.ExpressionDefineStubIndex.EXPR_DEFINE_KEY;

public class ExpressionDefineStubElementType extends IStubElementType<ExpressionDefineStub, ExpressionDefine> {

    public ExpressionDefineStubElementType() {
        super("Define", ExpressionLanguage.INSTANCE);
    }

    @Override
    public ExpressionDefine createPsi(@NotNull ExpressionDefineStub stub) {
        return new ExpressionDefineStubImpl(stub, this);
    }

    @Override
    public ExpressionDefineStub createStub(@NotNull ExpressionDefine psi, @SuppressWarnings("rawtypes") StubElement parentStub) {
        return new ExpressionDefineConcreteStub(parentStub, psi.getText(), "nope");
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "Expression.Define";
    }

    @Override
    public void serialize(@NotNull ExpressionDefineStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeUTFFast(stub.name());
    }

    @NotNull
    @Override
    public ExpressionDefineStub deserialize(@NotNull StubInputStream dataStream, @SuppressWarnings("rawtypes") StubElement parentStub) throws IOException {
        return new ExpressionDefineConcreteStub(parentStub, dataStream.readUTFFast(), "nope");
    }

    @Override
    public void indexStub(@NotNull ExpressionDefineStub stub, @NotNull IndexSink sink) {
        sink.occurrence(EXPR_DEFINE_KEY, stub.name());
    }
}
