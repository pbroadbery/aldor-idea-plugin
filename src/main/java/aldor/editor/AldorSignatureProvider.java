package aldor.editor;

import com.intellij.codeInsight.folding.impl.ElementSignatureProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by pab on 15/05/17.
 */
public class AldorSignatureProvider implements ElementSignatureProvider {
    @Nullable
    @Override
    public String getSignature(@NotNull PsiElement element) {
        return null;
    }

    @Nullable
    @Override
    public PsiElement restoreBySignature(@NotNull PsiFile file, @NotNull String signature, @Nullable StringBuilder processingInfoStorage) {
        return null;
    }
}
