package aldor.psi.elements;

import aldor.psi.AldorDeclare;
import aldor.psi.elements.AldorStubFactory.PsiElementFactory;
import aldor.psi.stub.AldorDeclareStub;
import aldor.psi.stub.impl.AldorDeclareConcreteStub;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxCodec;
import aldor.syntax.SyntaxPsiParser;
import aldor.util.StubCodec;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

import java.io.IOException;

public class AldorDeclareStubCodec implements PsiStubCodec<AldorDeclareStub, AldorDeclare> {
    private static final StubCodec<Syntax> syntaxCodec = new SyntaxCodec();
    private final PsiElementFactory<AldorDeclareStub, AldorDeclare> psiElementFactory;

    public AldorDeclareStubCodec(PsiElementFactory<AldorDeclareStub, AldorDeclare> psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    @Override
    public void encode(AldorDeclareStub stub, StubOutputStream dataStream) throws IOException {
        if (!stub.isDeclareOfId()) {
            dataStream.writeBoolean(false);
        } else {
            dataStream.writeBoolean(true);
            assert stub.declareIdName().isPresent();
            dataStream.writeName(stub.declareIdName().get());
            syntaxCodec.encode(dataStream, stub.lhsSyntax());
        }
    }

    @Override
    public AldorDeclareStub decode(StubInputStream dataStream, IStubElementType<AldorDeclareStub, AldorDeclare> eltType, StubElement<?> parentStub) throws IOException {
        boolean isDeclareOfId = dataStream.readBoolean();
        if (isDeclareOfId) {
            StringRef id = dataStream.readName();
            Syntax syntax = syntaxCodec.decode(dataStream);
            return new AldorDeclareConcreteStub(parentStub, eltType, syntax);
        }
        else {
            return new AldorDeclareConcreteStub(parentStub, eltType, null);
        }
    }

    @Override
    public AldorDeclare createPsi(IStubElementType<AldorDeclareStub, AldorDeclare> eltType, AldorDeclareStub stub) {
        return psiElementFactory.invoke(stub, eltType);
    }

    @Override
    public AldorDeclareStub createStub(StubElement<?> parentStub,
                                       IStubElementType<AldorDeclareStub, AldorDeclare> eltType, AldorDeclare aldorDeclare) {
        return new AldorDeclareConcreteStub(parentStub, eltType, SyntaxPsiParser.parse(aldorDeclare.lhs()));
    }

}
