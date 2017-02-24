package aldor.editor;

import aldor.psi.AldorIdentifier;
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

public class AldorIdentifierNavigationItem extends AbstractTreeNode<AldorIdentifier>
        implements PsiElementNavigationItem, DataProvider {
    @SuppressWarnings("AssignmentToSuperclassField")
    public AldorIdentifierNavigationItem(AldorIdentifier ident) {
        super(ident.getProject(), ident);
        myName = ident.getName();
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

    @Override
    public boolean isAlwaysLeaf() {
        return true;
    }

    @Override
    public void navigate(boolean requestFocus) {
        getValue().navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @NotNull
    @Override
    protected PresentationData createPresentation() {
        return new PresentationData(this.getValue().getText(),
                getValue().getContainingFile().getName(), AldorIcons.IDENTIFIER, null);
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


}
