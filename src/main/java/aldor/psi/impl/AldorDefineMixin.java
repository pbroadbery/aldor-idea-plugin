package aldor.psi.impl;

import aldor.psi.AldorDefineStubbing.AldorDefine;
import aldor.psi.AldorDefineStubbing.AldorDefineStub;
import aldor.psi.AldorIdentifier;
import aldor.psi.AldorPsiUtils;
import aldor.psi.elements.AldorDefineInfo;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.components.AldorDeclare;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Id;
import aldor.syntax.components.SyntaxUtils;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AldorDefineMixin extends StubBasedPsiElementBase<AldorDefineStub> implements AldorDefine {
    private static final Logger LOG = Logger.getInstance(AldorDefineMixin.class);
    private static final Key<Optional<Syntax>> cachedLhsSyntax = new Key<>("LhsSyntax");

    // Must be public - parser generator insists on it.
    public AldorDefineMixin(@NotNull ASTNode node) {
        super(node);
    }

    public AldorDefineMixin(AldorDefineStub stub, @SuppressWarnings("rawtypes") IStubElementType type) {
        super(stub, type);
    }

    @Override
    public AldorDefineStub createStub(IStubElementType<AldorDefineStub, AldorDefine> elementType, StubElement<?> parentStub) {
        String defineId = defineId().map(Id::symbol).orElse(null);
        boolean isTopLevelDefine = AldorPsiUtils.isTopLevel(getParent());
        AldorDefineInfo info = AldorDefineInfo.info(
                isTopLevelDefine ? AldorDefineInfo.Level.TOP: AldorDefineInfo.Level.INNER,
                AldorDefineInfo.Classification.OTHER);
        return new AldorDefineConcreteStub(parentStub, elementType, defineId, info);
    }

    @Override
    public Optional<AldorIdentifier> defineIdentifier() {
        return defineId().map(Id::aldorIdentifier);
    }

    private Optional<Id> defineId() {
        Optional<Syntax> syntaxMaybe = syntax();
        if (!syntaxMaybe.isPresent()) {
            return Optional.empty();
        }
        Syntax syntax = syntaxMaybe.get();
        if (syntax.is(AldorDeclare.class)) {
            syntax = syntax.as(AldorDeclare.class).lhs();
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

        if (!processor.execute(this, state)) {
            return false;
        }

        //noinspection ObjectEquality
        if (lastParent == lhs) {
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
        PsiElement lhs = getFirstChild();
        Optional<Syntax> syntax = this.getUserData(cachedLhsSyntax);
        if (syntax == null) {
            Syntax calculatedSyntax = SyntaxPsiParser.parse(lhs);
            syntax = Optional.ofNullable(calculatedSyntax);
            this.putUserDataIfAbsent(cachedLhsSyntax, syntax);
        }
        return syntax;
    }


    public static class AldorDefineConcreteStub extends StubBase<AldorDefine> implements AldorDefineStub {
        private final Syntax syntax;
        private final String defineId;
        private final AldorDefineInfo defineInfo;

        public AldorDefineConcreteStub(StubElement<?> parent,
                                       IStubElementType<AldorDefineStub, AldorDefine> type,
                                       String defineId, AldorDefineInfo defineInfo) {
            super(parent, type);
            syntax = null; // TODO: This one will be tricky
            this.defineId = defineId;
            this.defineInfo = defineInfo;
        }

        @Override
        public AldorDefine createPsi(IStubElementType<AldorDefineStub, AldorDefine> elementType) {
            return new AldorDefineMixin(this, elementType);
        }

        @Override
        public String defineId() {
            return defineId;
        }

        @Override
        public Syntax syntax() {
            return syntax;
        }

        @Override
        public AldorDefineInfo defineInfo() {
            return defineInfo;
        }

    }

}
