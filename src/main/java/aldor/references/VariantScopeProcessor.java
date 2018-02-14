package aldor.references;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class VariantScopeProcessor extends AbstractAldorScopeProcessor {
    private static final Logger LOG = Logger.getInstance(VariantScopeProcessor.class);
    private final List<Object> references = new ArrayList<>();

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        LOG.info("Searching scope "+ element);
        return super.execute(element, state);
    }

    @Override
    protected boolean executeDefinition(AldorDefine o, ResolveState state) {
        references.addAll(o.defineIdentifier().map(Collections::singleton).orElse(Collections.emptySet()));
        return true;
    }

    @Override
    protected boolean executeDeclare(AldorDeclare declare, ResolveState state) {
        if (declare.getName() != null) {
            references.add(declare);
        }
        return true;
    }

    @Override
    protected boolean executeIdentifier(AldorIdentifier o, ResolveState state) {
        references.add(o);
        return true;
    }

    public List<Object> references() {
        return references;
    }
}

