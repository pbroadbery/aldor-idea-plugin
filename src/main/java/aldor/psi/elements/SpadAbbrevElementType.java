package aldor.psi.elements;

import aldor.language.SpadLanguage;
import aldor.psi.SpadAbbrev;
import aldor.psi.stub.SpadAbbrevStub;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class SpadAbbrevElementType extends StubCodecElementType<SpadAbbrevStub, SpadAbbrev, SpadAbbrevElementType> {

    public static final StubIndexKey<String, SpadAbbrev> ABBREV_NAME_INDEX = StubIndexKey.createIndexKey("Spad.Abbrev.Name");
    public static final StubIndexKey<String, SpadAbbrev> ABBREV_ABBREV_INDEX = StubIndexKey.createIndexKey("Spad.Abbrev.Abbrev");

    public SpadAbbrevElementType(PsiStubCodec<SpadAbbrevStub, SpadAbbrev, SpadAbbrevElementType> spadAbbrevCodec) {
        super("SpadAbbrev", SpadLanguage.INSTANCE, spadAbbrevCodec);
    }

    @Override
    public void indexStub(@NotNull SpadAbbrevStub stub, @NotNull IndexSink sink) {
        sink.occurrence(ABBREV_ABBREV_INDEX, stub.info().abbrev());
        sink.occurrence(ABBREV_NAME_INDEX, stub.info().name());
    }

    @Override
    SpadAbbrevElementType toElType() {
        return this;
    }
}
