package aldor.references;

import aldor.editor.PsiElementToLookupElementMapping;
import aldor.psi.AldorDeclaration;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VariantScopeProcessor extends AbstractAldorScopeProcessor {
    private static final Logger LOG = Logger.getInstance(VariantScopeProcessor.class);
    private final PsiElementToLookupElementMapping lookupElementFactory;
    private final List<Object> references = new ArrayList<>();

    VariantScopeProcessor(PsiElementToLookupElementMapping lookupElementFactory) {
        this.lookupElementFactory = lookupElementFactory;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        LOG.info("Searching scope "+ element);
        return super.execute(element, state);
    }

    @Override
    protected boolean executeDeclaration(AldorDeclaration o, ResolveState state) {
        return true;
    }

    @Override
    protected boolean executeDefinition(AldorDefine o, ResolveState state) {
        switch (o.definitionType()) {
            case MACRO:
                references.add(lookupElementFactory.forMacro(o));
                break;
            case CONSTANT:
                references.add(lookupElementFactory.forConstant(o));
                break;
        }
        return true;
    }

    @Override
    protected boolean executeDeclare(AldorDeclare declare, ResolveState state) {
        if (declare.getName() != null) {
            references.add(lookupElementFactory.forDeclare(declare));
        }
        return true;
    }

    @Override
    protected boolean executeIdentifier(AldorIdentifier o, ResolveState state) {
        return true;
    }

    public List<Object> references() {
        return references;
    }

}

