package aldor.psi.elements;

import aldor.language.SpadLanguage;
import aldor.psi.SpadAbbrev;
import aldor.psi.stub.SpadAbbrevStub;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class SpadAbbrevElementType extends StubCodecElementType<SpadAbbrevStub, SpadAbbrev> {

    public static final StubIndexKey<String, SpadAbbrev> ABBREV_NAME_INDEX = StubIndexKey.createIndexKey("Spad.Abbrev.Name");
    public static final StubIndexKey<String, SpadAbbrev> ABBREV_ABBREV_INDEX = StubIndexKey.createIndexKey("Spad.Abbrev.Abbrev");

    public SpadAbbrevElementType(AldorStubFactory stubFactory) {
        super("SpadAbbrev", SpadLanguage.INSTANCE, stubFactory.abbrevCodec());
    }

    @Override
    public void indexStub(@NotNull SpadAbbrevStub stub, @NotNull IndexSink sink) {
        sink.occurrence(ABBREV_ABBREV_INDEX, stub.info().abbrev());
        sink.occurrence(ABBREV_NAME_INDEX, stub.info().name());
    }
}
