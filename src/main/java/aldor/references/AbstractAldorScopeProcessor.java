package aldor.references;

import aldor.psi.AldorColonExpr;
import aldor.psi.AldorDeclaration;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.ReturningAldorVisitor;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAldorScopeProcessor implements PsiScopeProcessor {

    // Return false to stop processing..
    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        return new ScopeVisitor(state).apply(element);
    }

    private class ScopeVisitor extends ReturningAldorVisitor<Boolean> {
        private final ResolveState state;

        ScopeVisitor(ResolveState state) {
            this.state = state;
        }

        @Override
        public void visitIdentifier(@NotNull AldorIdentifier o) {
            returnValue(executeIdentifier(o, state));
        }

        @Override
        public void visitDeclaration(@NotNull AldorDeclaration o) {
            returnValue(executeDeclaration(o, state));
        }

        @Override
        public void visitColonExpr(@NotNull AldorColonExpr o) {
            returnValue(executeDeclare((AldorDeclare) o, state));
        }

        @Override
        public void visitDeclare(@NotNull AldorDeclare o) {
            returnValue(executeDeclare(o, state));
        }

        @Override
        public void visitDefine(@NotNull AldorDefine o) {
            returnValue(executeDefinition(o, state));
        }

        @Override
        public void visitElement(PsiElement element) {
            returnValue(true);
        }
    }

    protected abstract boolean executeDeclaration(AldorDeclaration o, ResolveState state);

    protected abstract boolean executeDefinition(AldorDefine o, ResolveState state);

    protected abstract boolean executeDeclare(AldorDeclare o, ResolveState state);

    protected abstract boolean executeIdentifier(AldorIdentifier o, ResolveState state);

    @Nullable
    @Override
    public <T> T getHint(@NotNull Key<T> hintKey) {
        return null;
    }

    @Override
    public void handleEvent(@NotNull Event event, @Nullable Object associated) {

    }


}
