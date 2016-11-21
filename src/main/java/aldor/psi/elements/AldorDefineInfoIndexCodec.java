package aldor.psi.elements;

import aldor.psi.elements.AldorDefineInfo.Classification;
import aldor.psi.elements.AldorDefineInfo.Level;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

import java.io.IOException;
import java.util.Optional;

public class AldorDefineInfoIndexCodec implements IndexCodec<AldorDefineInfo> {

    @Override
    public void encode(StubOutputStream stream, AldorDefineInfo aldorDefineInfo) throws IOException {
        stream.writeName(aldorDefineInfo.classification().name());
        stream.writeName(aldorDefineInfo.level().name());
    }

    @Override
    public AldorDefineInfo decode(StubInputStream stream) throws IOException {
        Classification classification = Optional.ofNullable(stream.readName()).map(StringRef::getString).map(Classification::valueOf).orElse(null);
        Level level = Optional.ofNullable(stream.readName()).map(StringRef::getString).map(Level::valueOf).orElse(null);
        if ((level != null) && (classification != null)) {
            return AldorDefineInfo.info(level, classification);
        }
        throw new IOException("Error decoding Define Info");
    }
}
