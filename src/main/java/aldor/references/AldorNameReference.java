package aldor.references;

import aldor.psi.AldorIdentifier;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AldorNameReference extends PsiReferenceBase<AldorIdentifier> {


    public static final Object[] NO_VARIANTS = new Object[0];

    public AldorNameReference(@NotNull AldorIdentifier element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        AldorScopeProcessor scopeProcessor = new AldorScopeProcessor(getElement().getText());
        resolveAndWalk(scopeProcessor, getElement());

        return scopeProcessor.getResult();
    }

    @Override
    public TextRange getRangeInElement() {
        return new TextRange(0, myElement.getTextLength());
    }

    private void resolveAndWalk(PsiScopeProcessor scopeProcessor, PsiElement initial) {
        PsiElement thisScope = initial.getParent();
        PsiElement lastScope = initial;
        ResolveState state = ResolveState.initial();

        while (thisScope != null) {
            System.out.println("Looking at scope: " + thisScope);
            if (!thisScope.processDeclarations(scopeProcessor, state, lastScope, initial)) {
                break;
            }

            lastScope = thisScope;
            thisScope = thisScope.getParent();
        }
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return NO_VARIANTS;
    }

}
