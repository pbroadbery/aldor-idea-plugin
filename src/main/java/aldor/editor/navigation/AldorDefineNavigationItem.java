package aldor.editor.navigation;

import aldor.psi.AldorDefine;
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
import java.util.Optional;

public class AldorDefineNavigationItem  extends AbstractTreeNode<AldorDefine> implements PsiElementNavigationItem, DataProvider {

    @SuppressWarnings("AssignmentToSuperclassField")
    public AldorDefineNavigationItem(AldorDefine define) {
        super(define.getProject(), define);
        this.myName = define.defineIdentifier().map(PsiElement::getText).orElse("(Unknown)");
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
        Optional<AldorIdentifier> identifier = this.getValue().defineIdentifier();
        return new PresentationData(identifier.isPresent() ? identifier.get().getText() : "(missing)",
                getValue().getContainingFile().getName(), AldorIcons.IDENTIFIER, null);
    }


}

