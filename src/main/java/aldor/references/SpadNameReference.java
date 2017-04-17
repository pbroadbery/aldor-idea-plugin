package aldor.references;

import aldor.psi.AldorIdentifier;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        return scopeProcessor.getResult();
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
        return NO_VARIANTS;
    }
}
