package aldor.references;

import aldor.editor.ProjectPsiDefaults;
import aldor.editor.PsiElementToLookupElementMapping;
import aldor.psi.AldorIdentifier;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static aldor.references.FileScopeWalker.resolveAndWalk;

public class AldorNameReference extends PsiReferenceBase<AldorIdentifier> {
    private static final Logger LOG = Logger.getInstance(AldorNameReference.class);
    public static final Object[] NO_VARIANTS = new Object[0];

    public AldorNameReference(@NotNull AldorIdentifier element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        AldorScopeProcessor scopeProcessor = new AldorScopeProcessor(getElement().getText());
        resolveAndWalk(scopeProcessor, getElement());

        PsiElement result = scopeProcessor.getResult();
        if (result == null) {
            result = FileScopeWalker.lookupBySymbolFile(getElement());
        }
        if (result == null) {
            result = FileScopeWalker.lookupByIndex(getElement());
        }
        return result;
    }


    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return myElement.setName(newElementName);
    }

    // Suppress to keep logging - slightly worried that we rescan the codebase when not required.
    @SuppressWarnings({"EmptyMethod"})
    @Override
    public boolean isReferenceTo(PsiElement element) {
        //LOG.info("IsRefTo: " + this.getElement() + "@" + this.getElement().getContainingFile().getName() + ":" + getElement().getTextOffset()
        //        + " " + element + "@" + element.getContainingFile().getName() + ":" + element.getTextOffset());
        return super.isReferenceTo(element);
    }

    @NotNull
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
