package aldor.psi.index;

import aldor.psi.AldorDefine;
import aldor.psi.elements.AldorDefineElementType;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public final class AldorDefineTopLevelIndex extends StringStubIndexExtension<AldorDefine> {
    public static final AldorDefineTopLevelIndex instance = new AldorDefineTopLevelIndex();

    private AldorDefineTopLevelIndex() {
    }

    @NotNull
    @Override
    public StubIndexKey<String, AldorDefine> getKey() {
        return AldorDefineElementType.DEFINE_TOPLEVEL_INDEX;
    }

}
