package aldor.editor;

import aldor.psi.AldorDefineStubbing;
import aldor.ui.AldorIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.navigation.PsiElementNavigationItem;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class AldorDefineNavigationItem  extends AbstractTreeNode<AldorDefineStubbing.AldorDefine>  implements PsiElementNavigationItem, DataProvider {

    public AldorDefineNavigationItem(AldorDefineStubbing.AldorDefine define) {
        super(define.getProject(), define);
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    protected void update(PresentationData presentation) {
        getPresentation();
    }

    @Nullable
    @Override
    public PsiElement getTargetElement() {
        return getValue();
    }

    @Nullable
    @Override
    public Object getData(@NonNls String dataId) {
        if (CommonDataKeys.PSI_ELEMENT.is(dataId)) {
            return getValue();
        }
        return null;
    }

    @NotNull
    @Override
    protected PresentationData createPresentation() {
        return new PresentationData(this.getValue().defineIdentifier().get().getText(),
                getValue().getContainingFile().getName(), AldorIcons.IDENTIFIER, null);
    }


}
