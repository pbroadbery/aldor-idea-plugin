package aldor.psi.impl;

import aldor.psi.AldorDefine;
import aldor.psi.AldorId;
import aldor.psi.AldorIdentifier;
import aldor.psi.AldorPsiUtils;
import aldor.psi.stub.AldorDefineStub;
import aldor.references.FileScopeWalker;
import aldor.references.ScopeContext;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.Apply;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.Id;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AldorDefineMixin extends StubBasedPsiElementBase<AldorDefineStub> implements AldorDefine {
    private static final Logger LOG = Logger.getInstance(AldorDefineMixin.class);
    private static final Key<Optional<Syntax>> cachedLhsSyntax = new Key<>("LhsSyntax");

    // Must be public - parser generator insists on it.
    public AldorDefineMixin(@NotNull ASTNode node) {
        super(node);
    }

    public AldorDefineMixin(AldorDefineStub stub, IStubElementType<AldorDefineStub, AldorDefine> type) {
        super(stub, type);
    }

    @NotNull
    @Override
    public Optional<AldorIdentifier> defineIdentifier() {
        return defineId().map(Id::aldorIdentifier);
    }

    @Override
    public DefinitionType definitionType() {
        return DefinitionType.CONSTANT;
    }

    @Override
    public PsiElement implementation() {
        Optional<AldorId> implementationid = AldorPsiUtils.findUniqueIdentifier(rhs());
        if (implementationid.isPresent() && (implementationid.get().getReference() != null)) {
            PsiElement macro = implementationid.get().getReference().resolveMacro();
            if (macro instanceof AldorDefine) {
                return ((AldorDefine) macro).rhs();
            }
        }
        return rhs(); // FIXME!
    }

    @NotNull
    private Optional<Id> defineId() {
        Optional<Syntax> syntaxMaybe = syntax();
        if (!syntaxMaybe.isPresent()) {
            return Optional.empty();
        }
        Syntax syntax = syntaxMaybe.get();
        if (syntax.is(DeclareNode.class)) {
            syntax = syntax.as(DeclareNode.class).lhs();
        }
        while (syntax.is(Apply.class)) {
            syntax = syntax.as(Apply.class).operator();
        }
        if (syntax.is(Id.class)) {
            return Optional.of(syntax.as(Id.class));
        }
        return Optional.empty();
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        PsiElement lhs = getFirstChild();

        //noinspection ObjectEquality
        if (lastParent == lhs) {
            return true;
        }
        if (!processor.execute(this, state)) {
            return false;
        }
        if (state.get(FileScopeWalker.scopeContextKey) == ScopeContext.DeclBlock) {
            return true;
        }
        Optional<Syntax> syntax = syntax();

        if (syntax.isPresent()) {
            for (Syntax childScope: SyntaxUtils.childScopesForDefineLhs(syntax.get())) {
                if (!childScope.psiElement().processDeclarations(processor, state, lastParent, place)) {
                    return false;
                }
            }
        }

        return true;
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
