package aldor.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;


public interface AldorIterRepeatStatement extends PsiElement {
    @NotNull
    AldorIterators getIterators();

}
