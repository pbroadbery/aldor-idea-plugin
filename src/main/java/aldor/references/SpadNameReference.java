package aldor.references;

import aldor.editor.ProjectPsiDefaults;
import aldor.editor.PsiElementToLookupElementMapping;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static aldor.references.FileScopeWalker.resolveAndWalk;

public class SpadNameReference extends PsiReferenceBase<AldorIdentifier> {
    private static final Logger LOG = Logger.getInstance(SpadNameReference.class);
    public static final Object[] NO_VARIANTS = new Object[0];

    public SpadNameReference(@NotNull AldorIdentifier element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        AldorScopeProcessor scopeProcessor = new AldorScopeProcessor(getElement().getText());
        resolveAndWalk(scopeProcessor, getElement());

        PsiElement result = scopeProcessor.getResult();
        if (result == null) {
            result = resolveByTopLevelName();
        }

        return result;
    }

    private PsiElement resolveByTopLevelName() {
        Project project = getElement().getProject();
        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        Optional<VirtualFile> fileMaybe = Optional.ofNullable(getElement().getContainingFile().getVirtualFile());
        Optional<Module> module = fileMaybe.flatMap(file -> Optional.ofNullable(rootManager.getFileIndex().getModuleForFile(file, false)));
        Optional<GlobalSearchScope> scopeMaybe = module.map(GlobalSearchScope::moduleWithLibrariesScope);

        GlobalSearchScope scope = scopeMaybe.orElse(GlobalSearchScope.allScope(project));
        Collection<AldorDefine> topLevelDefines = AldorDefineTopLevelIndex.instance.get(getElement().getText(), project, scope);
        if (topLevelDefines.size() == 1) {
            return topLevelDefines.iterator().next();
        }
        return null;
    }


    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return myElement.setName(newElementName);
    }

    // Suppress to keep logging - slightly worried that we rescan the codebase when not required.
    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean isReferenceTo(PsiElement element) {
        //LOG.info("IsRefTo: " + this.getElement() + "@" + this.getElement().getContainingFile().getName() + ":" + getElement().getTextOffset()
        //        + " " + element + "@" + element.getContainingFile().getName() + ":" + element.getTextOffset());
        return super.isReferenceTo(element);
    }

    @Override
    public TextRange getRangeInElement() {
        return new TextRange(0, myElement.getTextLength());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        PsiElementToLookupElementMapping factory = ProjectPsiDefaults.lookupElementFactory(getElement().getProject());
        VariantScopeProcessor scopeProcessor = new VariantScopeProcessor(factory);
        resolveAndWalk(scopeProcessor, getElement());

        List<Object> result = scopeProcessor.references();
        //result.addAll(topLevelReferences());

        return result.toArray();
    }
}
