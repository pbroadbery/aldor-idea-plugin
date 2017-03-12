package aldor.psi.index;

import aldor.psi.AldorDeclare;
import aldor.psi.elements.AldorDeclareElementType;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public final class AldorDeclareTopIndex extends StringStubIndexExtension<AldorDeclare> {
    public static final AldorDeclareTopIndex instance = new AldorDeclareTopIndex();

    private AldorDeclareTopIndex() {
    }

    @NotNull
    @Override
    public StubIndexKey<String, AldorDeclare> getKey() {
        return AldorDeclareElementType.DECLARE_TOP_INDEX;
    }

}
