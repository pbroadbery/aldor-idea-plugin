package aldor.util;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings("unused")
public final class StubStreams {

    private static final StubCodec<String> STRING_CODEC = new StringCodec();

    public static StubCodec<String> stringCodec() {
        return STRING_CODEC;
    }

    private static class StringCodec implements StubCodec<String> {

        @NotNull
        @Override
        public Class<String> clzz() {
            return String.class;
        }

        @Override
        public void encode(StubOutputStream stream, String s) throws IOException {
            stream.writeUTFFast(s);
        }

        @Override
        public String decode(StubInputStream stream) throws IOException {
            return stream.readUTFFast();
        }
    }


}
