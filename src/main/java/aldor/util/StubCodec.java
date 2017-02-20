package aldor.util;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface StubCodec<T> {
    @NotNull
    Class<T> clzz();

    void encode(StubOutputStream stream, T t) throws IOException;
    T decode(StubInputStream stream) throws IOException;

}
