package aldor.editor;

import aldor.psi.AldorIdentifier;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class AldorIdentifierNavigationItem extends AbstractTreeNode<AldorIdentifier> {

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

    @NotNull
    @Override
    protected PresentationData createPresentation() {
        return new PresentationData(this.getValue().getText(),
                getValue().getContainingFile().getName(), IconUtil.getMoveUpIcon(), null);
    }

}
