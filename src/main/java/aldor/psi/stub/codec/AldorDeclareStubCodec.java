package aldor.psi.stub.codec;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorPsiUtils;
import aldor.psi.elements.AldorDeclareElementType;
import aldor.psi.elements.AldorStubFactory.PsiElementFactory;
import aldor.psi.elements.PsiStubCodec;
import aldor.psi.stub.AldorDeclareStub;
import aldor.psi.stub.impl.AldorDeclareConcreteStub;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxCodec;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.util.StubCodec;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

import java.io.IOException;
import java.util.Optional;

import static aldor.psi.AldorPsiUtils.containingBlock;

public class AldorDeclareStubCodec implements PsiStubCodec<AldorDeclareStub, AldorDeclare, AldorDeclareElementType> {
    private static final StubCodec<Syntax> syntaxCodec = new SyntaxCodec();
    private final PsiElementFactory<AldorDeclareStub, AldorDeclare> psiElementFactory;

    public AldorDeclareStubCodec(PsiElementFactory<AldorDeclareStub, AldorDeclare> psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    @Override
    public void encode(AldorDeclareStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeBoolean(stub.isCategoryDeclaration());
        if (!stub.isDeclareOfId()) {
            dataStream.writeBoolean(false);
            syntaxCodec.encode(dataStream, stub.syntax());
        } else {
            dataStream.writeBoolean(true);
            assert stub.declareIdName().isPresent();
            dataStream.writeName(stub.declareIdName().get());
            syntaxCodec.encode(dataStream, stub.syntax());
        }
        dataStream.writeBoolean(stub.exporter() != null);
        if (stub.exporter() != null) {
            syntaxCodec.encode(dataStream, stub.exporter());
        }
    }

    @Override
    public AldorDeclareStub decode(StubInputStream dataStream, AldorDeclareElementType eltType, StubElement<?> parentStub) throws IOException {
        boolean isCategoryDeclaration = dataStream.readBoolean();
        boolean isDeclareOfId = dataStream.readBoolean();

        AldorDeclareConcreteStub.Builder builder = AldorDeclareConcreteStub.builder();
        builder.setParent(parentStub)
                .setElementType(eltType)
                .setBlockType(isCategoryDeclaration ? AldorPsiUtils.WITH : AldorPsiUtils.BODY);

        if (isDeclareOfId) {
            //noinspection unused
            dataStream.readName(); // id (not used)
            Syntax syntax = syntaxCodec.decode(dataStream);
            builder.setSyntax(syntax);
        }
        else {
            Syntax syntax = syntaxCodec.decode(dataStream);
            builder.setSyntax(syntax);
        }
        boolean hasExporter = dataStream.readBoolean();
        if (hasExporter) {
            builder.setExporter(syntaxCodec.decode(dataStream));
        }
        return builder.build();
    }

    @Override
    public AldorDeclare createPsi(AldorDeclareElementType eltType, AldorDeclareStub stub) {
        return psiElementFactory.invoke(stub, eltType);
    }

    @Override
    public AldorDeclareStub createStub(StubElement<?> parentStub,
                                       AldorDeclareElementType eltType, AldorDeclare aldorDeclare) {
        Syntax syntax = SyntaxPsiParser.parse(aldorDeclare);
        AldorPsiUtils.ContainingBlockType<?> blockType = containingBlock(aldorDeclare).type();
        Optional<AldorDefine> definingForm = AldorPsiUtils.definingForm(aldorDeclare);
        Optional<Syntax> exporter = definingForm.map(form -> SyntaxUtils.typeName(SyntaxPsiParser.parse(form.lhs())));
        return new AldorDeclareConcreteStub.Builder()
                .setParent(parentStub)
                .setElementType(eltType)
                .setSyntax(syntax)
                .setBlockType(blockType)
                .setExporter(exporter.orElse(null))
                .build();
    }

}
