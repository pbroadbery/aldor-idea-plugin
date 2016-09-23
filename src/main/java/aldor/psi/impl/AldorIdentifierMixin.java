package aldor.psi.impl;

import aldor.psi.AldorIdentifier;
import aldor.references.AldorNameReference;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AldorIdentifierMixin extends ASTWrapperPsiElement implements AldorIdentifier {

    protected AldorIdentifierMixin(@NotNull ASTNode node) {
        super(node);
    }


    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        //final AldorElementGenerator generator = new AldorElementGenerator(getProject());
        // Strip only both quotes in case user wants some exotic name like key'
        //getNameElement().replace(generator.createStringLiteral(StringUtil.unquoteString(name)));
        return this;
    }

    @Override
    public PsiReference getReference() {
        return new AldorNameReference(this);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        final PsiReference[] fromProviders = ReferenceProvidersRegistry.getReferencesFromProviders(this);
        return ArrayUtil.prepend(new AldorNameReference(this), fromProviders);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        return processor.execute(this, state);
    }
}
