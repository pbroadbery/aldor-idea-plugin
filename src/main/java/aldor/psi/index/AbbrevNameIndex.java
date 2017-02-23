package aldor.psi.index;

import aldor.psi.SpadAbbrev;
import aldor.psi.elements.SpadAbbrevElementType;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public final class AbbrevNameIndex extends StringStubIndexExtension<SpadAbbrev> {
    public static final AbbrevNameIndex instance = new AbbrevNameIndex();

    private AbbrevNameIndex() {
    }

    @NotNull
    @Override
    public StubIndexKey<String, SpadAbbrev> getKey() {
        return SpadAbbrevElementType.ABBREV_NAME_INDEX;
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
