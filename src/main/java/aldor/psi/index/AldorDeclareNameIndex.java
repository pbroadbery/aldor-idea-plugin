package aldor.psi.index;

import aldor.psi.AldorDeclare;
import aldor.psi.elements.AldorDeclareElementType;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public final class AldorDeclareNameIndex extends StringStubIndexExtension<AldorDeclare> {
    public static final AldorDeclareNameIndex instance = new AldorDeclareNameIndex();

    private AldorDeclareNameIndex() {
    }

    @NotNull
    @Override
    public StubIndexKey<String, AldorDeclare> getKey() {
        return AldorDeclareElementType.DECLARE_NAME_INDEX;
    }

}
