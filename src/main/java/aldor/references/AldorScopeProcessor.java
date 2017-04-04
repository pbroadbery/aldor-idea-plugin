package aldor.references;

import aldor.psi.AldorColonExpr;
import aldor.psi.AldorDeclaration;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.impl.ReturningAldorVisitor;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.components.Id;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AldorScopeProcessor implements PsiScopeProcessor {
    private final List<PsiElement> myResultList;
    private final String name;

    public AldorScopeProcessor(String name) {
        myResultList = new ArrayList<>();
        this.name = name;
    }

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
            returnValue(executeDeclare((AldorDeclare) o, state));
        }

        @Override
        public void visitColonExpr(@NotNull AldorColonExpr o) {
            returnValue(executeDeclare((AldorDeclare) o, state));
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

    private boolean executeDefinition(AldorDefine o, ResolveState state) {
        Optional<AldorIdentifier> identMaybe = o.defineIdentifier();
        if (identMaybe.isPresent() && identMaybe.get().getText().equals(this.name)) {
            this.myResultList.add(o);
            return false;
        }
        return true;
    }

    private boolean executeDeclare(AldorDeclare declare, ResolveState state) {
        Syntax lhs = SyntaxPsiParser.parse(declare.lhs());
        if ((lhs != null) && lhs.is(Id.class)) {
            if (this.name.equals(lhs.as(Id.class).symbol())) {
                this.myResultList.add(declare);
                return false;
            }
        }
        return true;
    }

    private boolean executeIdentifier(PsiElement id, ResolveState state) {
        if (this.name.equals(id.getText())) {
            // FIXME: returning the id is probably incorrect
            // - The outer define or declare is more suitable.  Unfortunately it might not exist..
            this.myResultList.add(id);
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public <T> T getHint(@NotNull Key<T> hintKey) {
        return null;
    }

    @Override
    public void handleEvent(@NotNull Event event, @Nullable Object associated) {

    }

    @Nullable
    public PsiElement getResult() {
        if (myResultList.isEmpty()) {
            return null;
        }
        else {
            return myResultList.get(0);
        }
    }

    @Override
    public String toString() {
        return "{Proc: " + name + " " + myResultList + "}";
    }
}
