package aldor.psi.index;

import aldor.psi.AldorDefine;
import aldor.psi.elements.AldorDefineElementType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class AldorDefineNameIndex extends StringStubIndexExtension<AldorDefine> {
    public static final AldorDefineNameIndex instance = new AldorDefineNameIndex();

    private AldorDefineNameIndex() {
    }

    @NotNull
    @Override
    public StubIndexKey<String, AldorDefine> getKey() {
        return AldorDefineElementType.DEFINE_NAME_INDEX;
    }

    @Override
    public Collection<AldorDefine> get(@NotNull String s, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return super.get(s, project, scope);
    }
}
