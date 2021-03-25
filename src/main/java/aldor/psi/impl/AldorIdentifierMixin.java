package aldor.psi.impl;

import aldor.language.SpadLanguage;
import aldor.lexer.AldorTokenTypes;
import aldor.psi.AldorElementFactory;
import aldor.psi.AldorIdentifier;
import aldor.references.AldorNameReference;
import aldor.references.AldorReference;
import aldor.references.SpadNameReference;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorIdentifierMixin extends ASTWrapperPsiElement implements AldorIdentifier {
    private static final Logger LOG = Logger.getInstance(AldorIdentifierMixin.class);

    protected AldorIdentifierMixin(@NotNull ASTNode node) {
        super(node);
    }

    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public PsiElement setName(@NonNls @NotNull String newName) throws IncorrectOperationException {
        AldorIdentifier newIdentifier = AldorElementFactory.createIdentifier(getProject(), newName);
        ASTNode ref = getNode().findChildByType(AldorTokenTypes.TK_Id);
        ASTNode newRef = newIdentifier.getNode().findChildByType(AldorTokenTypes.TK_Id);
        if ((ref != null) && (newRef != null)) {
            getNode().replaceChild(ref, newRef);
        }
        return this;
    }

    @Override
    public String getName() {
        return getText();
    }

    @Nullable
    @Override
    public AldorReference getReference() {
        return getCoreReference();
    }

    private AldorReference getCoreReference() {
        if (SpadLanguage.INSTANCE.equals(getContainingFile().getLanguage())) {
            return new SpadNameReference(this);
        }
        return new AldorNameReference(this);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        final PsiReference[] fromProviders = ReferenceProvidersRegistry.getReferencesFromProviders(this);

        PsiReference ref = getCoreReference();
        return ArrayUtil.prepend(ref, fromProviders);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        return processor.execute(this, state);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return this.findChildByType(AldorTokenTypes.TK_Id);
    }



}
