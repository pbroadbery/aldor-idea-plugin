package aldor.psi.index;

import aldor.psi.AldorDefine;
import aldor.psi.elements.AldorDefineElementType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collection;
import java.util.stream.Collectors;

public final class AldorDefineNameIndex extends StringStubIndexExtension<AldorDefine> {
    public static final AldorDefineNameIndex instance = new AldorDefineNameIndex();

    private AldorDefineNameIndex() {
    }

    @NotNull
    @Override
    public StubIndexKey<String, AldorDefine> getKey() {
        return AldorDefineElementType.DEFINE_NAME_INDEX;
    }

    @VisibleForTesting
    public Collection<String> getAllKeys(Project project, GlobalSearchScope scope) {
        return getAllKeys(project).stream().filter(k -> !get(k, project, scope).isEmpty()).collect(Collectors.toSet());
    }

}
