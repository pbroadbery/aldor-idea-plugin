package aldor.psi.index;

import aldor.psi.SpadAbbrev;
import aldor.psi.elements.SpadAbbrevElementType;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public final class AbbrevAbbrevIndex extends StringStubIndexExtension<SpadAbbrev> {
    public static final AbbrevAbbrevIndex instance = new AbbrevAbbrevIndex();

    private AbbrevAbbrevIndex() {
    }

    @NotNull
    @Override
    public StubIndexKey<String, SpadAbbrev> getKey() {
        return SpadAbbrevElementType.ABBREV_ABBREV_INDEX;
    }
}
