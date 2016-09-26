package aldor.references;

import aldor.psi.AldorIdentifier;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AldorScopeProcessor implements PsiScopeProcessor {
    private final List<AldorIdentifier> myResultList;
    private final String name;

    public AldorScopeProcessor(String name) {
        myResultList = new ArrayList<>();
        this.name = name;
    }

    // Return false to stop processing..
    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        if (!(element instanceof AldorIdentifier)) {
            return true;
        }
        AldorIdentifier id = (AldorIdentifier) element;
        if (this.name.equals(id.getText())) {
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

}
