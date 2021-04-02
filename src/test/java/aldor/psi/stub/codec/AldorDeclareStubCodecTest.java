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
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SimpleStringEnumerator;
import com.intellij.mock.MockApplication;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.testFramework.PlatformLiteFixture;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.io.AbstractStringEnumerator;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AldorDeclareStubCodecTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testEncode() throws Exception {
        AldorDeclareStub stub = new AldorDeclareConcreteStub.Builder()
                                        .setBlockType(AldorPsiUtils.WITH)
                                        .setElementType(null)
                                        .setExporter(Id.createMissingId(AldorTokenTypes.TK_Id, "Foo"))
                                        .setParent(null)
                                        .setSyntax(Id.createMissingId(AldorTokenTypes.TK_Id, "op")).build();

        AldorDeclareStub inStub = encodeDecode(stub);
        Assert.assertTrue(SyntaxUtils.match(stub.syntax(), inStub.syntax()));
        Assert.assertTrue(SyntaxUtils.match(stub.exporter(), inStub.exporter()));
        Assert.assertNull(inStub.getParentStub());
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
