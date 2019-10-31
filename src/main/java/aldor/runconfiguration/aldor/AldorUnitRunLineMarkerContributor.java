package aldor.runconfiguration.aldor;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AldorUnitRunLineMarkerContributor extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        // See TestRunLineMarkerProvider
        return null;
    }
}
