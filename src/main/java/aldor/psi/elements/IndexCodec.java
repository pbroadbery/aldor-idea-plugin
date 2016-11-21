package aldor.psi.elements;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

import java.io.IOException;

public interface IndexCodec<T> {
    void encode(StubOutputStream stream, T t) throws IOException;
    T decode(StubInputStream stream) throws IOException;

}
