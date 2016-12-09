package aldor.references;

import aldor.psi.AldorDefineStubbing.AldorDefine;
import aldor.psi.SpadAbbrevStubbing.SpadAbbrev;
import aldor.psi.index.AldorDefineTopLevelIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static aldor.psi.SpadAbbrevStubbing.AbbrevInfo;

public class SpadAbbrevReference extends PsiReferenceBase<SpadAbbrev> {
    private static final Object [] NO_VARIANTS = new Object[0];
    private final AbbrevInfo info;

    public SpadAbbrevReference(SpadAbbrev abbrev) {
        super(abbrev);
        this.info = abbrev.abbrevInfo();
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        if (info.isError()) {
            return null;
        }
        Project project = getElement().getProject();
        Collection<AldorDefine> items = AldorDefineTopLevelIndex.instance.get(info.name(), project, GlobalSearchScope.allScope(project));
        if (items.size() != 1) {
            return null;
        }
        return items.iterator().next();
    }

    @Override
    protected TextRange calculateDefaultRangeInElement() {
        if (info.isError()) {
            return new TextRange(0, 0);
        }
        int startIndex = info.nameIndex();
        int endIndex = startIndex + info.name().length();
        return new TextRange(startIndex, endIndex);
    }

    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return myElement.setName(newElementName);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return NO_VARIANTS;
    }
}
