package aldor.references;

import aldor.psi.AldorDeclaration;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.components.Id;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AldorScopeProcessor extends AbstractAldorScopeProcessor {
    private final List<PsiElement> myResultList;
    private final String name;

    public AldorScopeProcessor(String name) {
        myResultList = new ArrayList<>();
        this.name = name;
    }

    @Override
    protected boolean executeDeclaration(AldorDeclaration o, ResolveState state) {
        return true;
    }

    @Override
    protected boolean executeDefinition(AldorDefine o, ResolveState state) {
        Optional<AldorIdentifier> identMaybe = o.defineIdentifier();
        if (identMaybe.isPresent() && identMaybe.get().getText().equals(this.name)) {
            this.myResultList.add(o);
            return false;
        }
        return true;
    }

    @Override
    protected boolean executeDeclare(AldorDeclare declare, ResolveState state) {
        Syntax lhs = SyntaxPsiParser.parse(declare.lhs());
        if ((lhs != null) && lhs.is(Id.class)) {
            if (this.name.equals(lhs.as(Id.class).symbol())) {
                this.myResultList.add(declare);
                return false;
            }
        }
        return true;
    }

    @Override
    protected  boolean executeIdentifier(AldorIdentifier id, ResolveState state) {
        if (this.name.equals(id.getText())) {
            // FIXME: returning the id is probably incorrect
            // - The outer define or declare is more suitable.  Unfortunately it might not exist..
            this.myResultList.add(id);
            return false;
        }
        return true;
    }

    @Nullable
    public PsiElement getResult() {
        if (myResultList.isEmpty()) {
            return null;
        } else {
            return myResultList.get(0);
        }
    }

    @Override
    public String toString() {
        return "{Proc: " + name + " " + myResultList + "}";
    }
}
