package aldor.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AldorAnyCollection extends PsiElement {
    @NotNull
    List<AldorIterator> getIteratorList();
}
