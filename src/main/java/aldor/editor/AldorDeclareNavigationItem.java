package aldor.editor;

import aldor.psi.AldorDeclare;
import aldor.psi.stub.AldorDeclareStub;
import aldor.syntax.DeclareFunctions;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.components.AbstractId;
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

public class AldorDeclareNavigationItem extends AbstractTreeNode<AldorDeclare> implements PsiElementNavigationItem, DataProvider {
    private static final SyntaxPrinter printer = SyntaxPrinter.instance();

    @SuppressWarnings("AssignmentToSuperclassField")
    public AldorDeclareNavigationItem(AldorDeclare declare) {
        super(declare.getProject(), declare);
        AldorDeclareStub stub = declare.getGreenStub();
        if (stub == null) {
            this.myName = DeclareFunctions.declareId(SyntaxPsiParser.parse(declare.getFirstChild())).map(AbstractId::symbol).orElse("(unknown)");
        } else {
            this.myName = stub.declareIdName().orElse("(unknown)");
        }
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
        AldorDeclare declare = this.getValue();
        AldorDeclareStub stub = declare.getGreenStub();
        Syntax declareType = (stub != null) ? stub.declareType() : SyntaxPsiParser.parse(declare.rhs());
        return new PresentationData(this.myName + ": " + printer.toString(declareType),
                getValue().getContainingFile().getName(), AldorIcons.DECLARE_ICON, null);
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
