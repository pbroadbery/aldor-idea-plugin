package aldor.references;

import aldor.psi.AldorDeclaration;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.syntax.DeclareFunctions;
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
    private final Options options;
    private final String name;

    public AldorScopeProcessor(String name) {
        this(Options.ANY, name);
    }

    public AldorScopeProcessor(Options options, String name) {
        this.myResultList = new ArrayList<>();
        this.options = options;
        this.name = name;
    }

    @Override
    protected boolean executeDeclaration(AldorDeclaration o, ResolveState state) {
        return true;
    }

    @Override
    protected boolean executeDefinition(AldorDefine o, ResolveState state) {
        Optional<AldorIdentifier> identMaybe = o.defineIdentifier();
        if (identMaybe.isPresent() && identMaybe.get().getText().equals(this.name) && options.isAccepted(o.definitionType())) {
            this.myResultList.add(o);
            return false;
        }
        return true;
    }

    @Override
    protected boolean executeDeclare(AldorDeclare declare, ResolveState state) {
        if (!options.includesDeclare()) {
            return true;
        }
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


    public static class Options {
        public static final Options MACRO = new Options(AldorDefine.DefinitionType.MACRO);
        static final Options ANY = new Options(null);

        @Nullable
        private final AldorDefine.DefinitionType definitionType;

        Options(@Nullable AldorDefine.DefinitionType definitionType) {
            this.definitionType = definitionType;
        }

        public boolean includesDeclare() {
            return definitionType == null;
        }

        public boolean isAccepted(AldorDefine.DefinitionType definitionType) {
            return (this.definitionType == null) || (definitionType == this.definitionType);
        }
    }
}
