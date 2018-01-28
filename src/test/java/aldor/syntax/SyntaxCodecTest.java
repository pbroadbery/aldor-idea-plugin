package aldor.syntax;

import aldor.parser.ParserFunctions;
import aldor.parser.SwingThreadTestRule;
import aldor.syntax.components.Other;
import aldor.test_util.LightPlatformJUnit4TestRule;
import aldor.util.StubCodec;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.io.AbstractStringEnumerator;
import org.jetbrains.annotations.Nullable;
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
                    .around(new SwingThreadTestRule());

    @Test
    public void testStubCodec() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String text = "F -> G";
        Syntax syntax = parseToSyntax(text);
        Syntax inSyntax = encodeDecode(byteArrayOutputStream, syntax);
        assertEquals(syntax.toString(), inSyntax.toString());
    }

    @Test
    public void testStubCodec2() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String text = "(A, B) -> C";
        Syntax syntax = parseToSyntax(text);
        Syntax inSyntax = encodeDecode(byteArrayOutputStream, syntax);
        assertEquals(syntax.toString(), inSyntax.toString());
    }


    private Syntax encodeDecode(ByteArrayOutputStream baos, Syntax syntax) throws IOException {
        System.out.println("Syntax is: " + syntax);
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


    private static final class SimpleStringEnumerator implements AbstractStringEnumerator {
        private final BiMap<Integer, String> stringForIndex;

        private SimpleStringEnumerator() {
            this.stringForIndex = HashBiMap.create();
        }

        @Override
        public void markCorrupted() {

        }

        @Override
        public boolean isDirty() {
            return false;
        }

        @Override
        public void force() {

        }

        @Override
        public int enumerate(@Nullable String value) {
            if (stringForIndex.inverse().containsKey(value)) {
                return stringForIndex.inverse().get(value);
            }
            int idx = stringForIndex.size() + 1;
            stringForIndex.put(idx, value);
            System.out.println("Adding: " + idx + " -> " + value);
            return idx;
        }

        @Nullable
        @Override
        public String valueOf(int idx) {
            return stringForIndex.get(idx);
        }

        @Override
        public void close() {

        }
    }
}
