package aldor.syntax;

import aldor.parser.ParserFunctions;
import aldor.syntax.components.Other;
import aldor.test_util.JUnits;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.test_util.SimpleStringEnumerator;
import aldor.util.StubCodec;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.io.AbstractStringEnumerator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SyntaxCodecTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(JUnits.swingThreadTestRule());

    @Test
    public void testStubCodec() throws IOException {
        String text = "F -> G";
        Syntax syntax = parseToSyntax(text);
        Syntax inSyntax = encodeDecode(syntax);
        assertEquals(syntax.toString(), inSyntax.toString());
    }

    @Test
    public void testStubCodec2() throws IOException {
        String text = "(A, B) -> C";
        Syntax syntax = parseToSyntax(text);
        Syntax inSyntax = encodeDecode(syntax);
        assertEquals(syntax.toString(), inSyntax.toString());
    }


    private Syntax encodeDecode(Syntax syntax) throws IOException {
        System.out.println("Syntax is: " + syntax);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StubCodec<Syntax> codec = new SyntaxCodec();
        AbstractStringEnumerator stringEnumerator = new SimpleStringEnumerator();
        StubOutputStream stubOutputStream = new StubOutputStream(baos, stringEnumerator);
        codec.encode(stubOutputStream, syntax);
        stubOutputStream.close();

        byte[] bytes = baos.toByteArray();
        Syntax inSyntax;
        try (InputStream inStream = new ByteArrayInputStream(bytes)) {
            inSyntax = codec.decode(new StubInputStream(inStream, stringEnumerator)) ;
        }
        return inSyntax;
    }


    @Test
    public void testMissingCodec() throws IOException {
        //noinspection LimitedScopeInnerClass
        class RandomSubclass extends Other {
            RandomSubclass() {
                super(null);
            }
        }

        Syntax syntax = new RandomSubclass();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StubCodec<Syntax> codec = new SyntaxCodec();
        AbstractStringEnumerator stringEnumerator = new SimpleStringEnumerator();
        StubOutputStream stubOutputStream = new StubOutputStream(byteArrayOutputStream, stringEnumerator);
        codec.encode(stubOutputStream, syntax);
        stubOutputStream.close();

        byte[] bytes = byteArrayOutputStream.toByteArray();
        Syntax inSyntax;
        try (InputStream inStream = new ByteArrayInputStream(bytes)) {
            inSyntax = codec.decode(new StubInputStream(inStream, stringEnumerator)) ;
        }
        assertTrue(inSyntax.toString().contains("RandomSubclass"));
    }

    private Syntax parseToSyntax(CharSequence text) {
        return ParserFunctions.parseToSyntax(this.testFixture.getProject(), text);
    }


}
