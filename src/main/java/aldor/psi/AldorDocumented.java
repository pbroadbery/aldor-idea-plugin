package aldor.psi;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface AldorDocumented extends PsiElement {
    @NotNull
    default Collection<PsiElement> documentationNodes() {
        List<PsiElement> docNodes = Lists.newArrayList();
        for (PsiElement child: this.getChildren()) {
            if ((child instanceof AldorPreDocument) || (child instanceof AldorPostDocument)) {
                docNodes.add(child);
            }
        }
        //noinspection unchecked
        return docNodes;
    }
}
