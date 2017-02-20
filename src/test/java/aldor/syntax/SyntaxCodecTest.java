package aldor.syntax;

import aldor.parser.LightPlatformJUnit4TestRule;
import aldor.parser.ParserFunctions;
import aldor.parser.SwingThreadTestRule;
import aldor.psi.elements.AldorTypes;
import aldor.util.StubCodec;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.psi.PsiElement;
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

public class SyntaxCodecTest {
    private final CodeInsightTestFixture testFixture = LightPlatformJUnit4TestRule.createFixture(null);

    @Rule
    public final TestRule platformTestRule =
            RuleChain.emptyRuleChain()
                    .around(new LightPlatformJUnit4TestRule(testFixture, ""))
                    .around(new SwingThreadTestRule());

    @Test
    public void testStubCodec() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Syntax syntax = parseToSyntax("F -> G");
        System.out.println("Syntax is: " + syntax);
        StubCodec<Syntax> codec = new SyntaxCodec();
        AbstractStringEnumerator stringEnumerator = new SimpleStringEnumerator();
        StubOutputStream outputStream1 = new StubOutputStream(outputStream, stringEnumerator);
        codec.encode(outputStream1, syntax);
        outputStream1.close();

        byte[] bytes = outputStream.toByteArray();
        Syntax inSyntax;
        try (InputStream inStream = new ByteArrayInputStream(bytes)) {
            inSyntax = codec.decode(new StubInputStream(inStream, stringEnumerator)) ;
        }
        assertEquals(syntax.toString(), inSyntax.toString());
    }


    private Syntax parseToSyntax(CharSequence text) {
        PsiElement element = parseText(text);
        return SyntaxPsiParser.parse(element);
    }

    private PsiElement parseText(CharSequence text) {
        return ParserFunctions.parseAldorText(testFixture.getProject(), text, AldorTypes.TOP_LEVEL);
    }


    private static class SimpleStringEnumerator implements AbstractStringEnumerator {
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
        public int enumerate(@Nullable String value) throws IOException {
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
        public String valueOf(int idx) throws IOException {
            System.out.println("Value: " + idx + " " + stringForIndex.get(idx));
            return stringForIndex.get(idx);
        }

        @Override
        public void close() throws IOException {

        }
    }
}
