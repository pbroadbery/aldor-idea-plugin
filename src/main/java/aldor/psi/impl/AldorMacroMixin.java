package aldor.psi.impl;

import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.AldorPsiUtils;
import aldor.psi.AldorVisitor;
import aldor.psi.stub.AldorDefineStub;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Id;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AldorMacroMixin extends StubBasedPsiElementBase<AldorDefineStub> implements AldorDefine {
    private static final Key<Optional<Syntax>> cachedLhsSyntax = new Key<>("LhsSyntax");
    private static final long serialVersionUID = 5335530555698048661L;

    public AldorMacroMixin(@NotNull AldorDefineStub stub, @NotNull IStubElementType<AldorDefineStub, AldorDefine> nodeType) {
        super(stub, nodeType);
    }

    public AldorMacroMixin(@NotNull ASTNode node) {
        super(node);
    }

    public AldorMacroMixin(AldorDefineStub stub, IElementType nodeType, ASTNode node) {
        super(stub, nodeType, node);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof AldorVisitor) accept((AldorVisitor)visitor);
        else super.accept(visitor);
    }

    public void accept(@NotNull AldorVisitor aldorVisitor) {
        aldorVisitor.visitDefine(this);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        PsiElement lhs = getFirstChild();

        //noinspection ObjectEquality
        if (lastParent == lhs) {
            return true;
        }

        if (!processor.execute(this, state)) {
            return false;
        }

        Optional<Syntax> syntaxMaybe = syntax();

        Optional<Syntax> childScope = syntaxMaybe.flatMap(SyntaxUtils::childScopeForMacroLhs);
        Optional<Boolean> ret = childScope.map(scope -> scope.psiElement().processDeclarations(processor, state, lastParent, place));

        return ret.orElse(true);
    }

    @NotNull
    @Override
    public Optional<AldorIdentifier> defineIdentifier() {
        return defineId().map(Id::aldorIdentifier);
    }

    @Override
    public DefinitionType definitionType() {
        return DefinitionType.MACRO;
    }

    @Override
    public PsiElement implementation() {
        return rhs();
    }

    private Optional<Id> defineId() {
        Optional<Syntax> syntaxMaybe = syntax();
        if (!syntaxMaybe.isPresent()) {
            return Optional.empty();
        }
        Syntax syntax = syntaxMaybe.get();
        if (syntax.is(Apply.class)) {
            syntax = syntax.as(Apply.class).operator();
        }
        if (syntax.is(Id.class)) {
            return Optional.of(syntax.as(Id.class));
        }
        return Optional.empty();
    }


    @NotNull
    private Optional<Syntax> syntax() {
        Optional<Syntax> syntax = this.getUserData(cachedLhsSyntax);
        //noinspection OptionalAssignedToNull
        if (syntax == null) {
            PsiElement lhs = getFirstChild();
            Syntax calculatedSyntax = SyntaxPsiParser.parse(lhs);
            syntax = Optional.ofNullable(calculatedSyntax);
            this.putUserDataIfAbsent(cachedLhsSyntax, syntax);
        }
        return syntax;
    }

    @Override
    public String getName() {
        return this.defineIdentifier().map(PsiElement::getText).orElse(null);
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        AldorPsiUtils.ContainingBlock<?> block = AldorPsiUtils.containingBlock(this);
        return new LocalSearchScope(block.element());
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return this.defineIdentifier().orElse(null);
    }

    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        if (this.defineIdentifier().isPresent()) {
            this.defineIdentifier().get().setName(name);
        }
        return this;
    }
}
