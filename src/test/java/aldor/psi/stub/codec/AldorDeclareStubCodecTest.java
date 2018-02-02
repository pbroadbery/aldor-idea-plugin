package aldor.psi.stub.codec;

import aldor.lexer.AldorTokenTypes;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorPsiUtils;
import aldor.psi.elements.AldorDeclareElementType;
import aldor.psi.elements.AldorStubFactory;
import aldor.psi.elements.AldorStubFactoryImpl;
import aldor.psi.elements.PsiStubCodec;
import aldor.psi.impl.AldorDeclPartImpl;
import aldor.psi.stub.AldorDeclareStub;
import aldor.psi.stub.impl.AldorDeclareConcreteStub;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.Id;
import aldor.test_util.SimpleStringEnumerator;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.AbstractStringEnumerator;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;

public class AldorDeclareStubCodecTest {
    @Test
    public void testEncode() throws Exception {

        AldorDeclareStub stub = AldorDeclareConcreteStub.builder()
                                        .setBlockType(AldorPsiUtils.WITH)
                                        .setElementType(null)
                                        .setExporter(Id.createMissingId(AldorTokenTypes.TK_Id, "Foo"))
                                        .setParent(null)
                                        .setSyntax(Id.createMissingId(AldorTokenTypes.TK_Id, "op")).build();

        AldorDeclareStub inStub = encodeDecode(stub);
        assertTrue(SyntaxUtils.match(stub.syntax(), inStub.syntax()));
        assertTrue(SyntaxUtils.match(stub.exporter(), inStub.exporter()));
        assertNull(inStub.getParentStub());
    }


    private AldorDeclareStub encodeDecode(AldorDeclareStub stub) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AldorStubFactory stubFactory = new AldorStubFactoryImpl();
        AbstractStringEnumerator stringEnumerator = new SimpleStringEnumerator();

        PsiStubCodec<AldorDeclareStub, AldorDeclare, AldorDeclareElementType> codec = stubFactory.declareCodec(AldorDeclPartImpl::new);

        StubOutputStream stubOutputStream = new StubOutputStream(baos, stringEnumerator);
        codec.encode(stub, stubOutputStream);
        stubOutputStream.close();

        byte[] bytes = baos.toByteArray();
        AldorDeclareStub inStub;
        try (InputStream inStream = new ByteArrayInputStream(bytes)) {
            inStub = codec.decode(new StubInputStream(inStream, stringEnumerator), null, null) ;
        }
        return inStub;
    }


}
