package aldor.psi.index;

import aldor.psi.AldorDefineStubbing.AldorDefine;
import aldor.psi.elements.AldorDefineElementType;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public final class AldorDefineNameIndex extends StringStubIndexExtension<AldorDefine> {
    public static final AldorDefineNameIndex instance = new AldorDefineNameIndex();

    private AldorDefineNameIndex() {
    }

    @NotNull
    @Override
    public StubIndexKey<String, AldorDefine> getKey() {
        return AldorDefineElementType.DEFINE_NAME_INDEX;
    }

}
