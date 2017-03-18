package aldor.psi.impl;

import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
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
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AldorMacroMixin extends StubBasedPsiElementBase<AldorDefineStub> implements AldorDefine {
    private static final Key<Optional<Syntax>> cachedLhsSyntax = new Key<>("LhsSyntax");

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
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        PsiElement lhs = getFirstChild();

        if (!processor.execute(this, state)) {
            return false;
        }

        //noinspection ObjectEquality
        if (lastParent == lhs) {
            return true;
        }
        Optional<Syntax> syntaxMaybe = syntax();

        Optional<Syntax> childScope = syntaxMaybe.flatMap(SyntaxUtils::childScopeForMacroLhs);
        Optional<Boolean> ret = childScope.map(scope -> scope.psiElement().processDeclarations(processor, state, lastParent, place));

        return ret.orElse(true);
    }

    @Override
    public Optional<AldorIdentifier> defineIdentifier() {
        return defineId().map(Id::aldorIdentifier);
    }

    @Override
    public DefinitionType definitionType() {
        return DefinitionType.MACRO;
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
        if (syntax == null) {
            PsiElement lhs = getFirstChild();
            Syntax calculatedSyntax = SyntaxPsiParser.parse(lhs);
            syntax = Optional.ofNullable(calculatedSyntax);
            this.putUserDataIfAbsent(cachedLhsSyntax, syntax);
        }
        return syntax;
    }

}
