package aldor.hierarchy;

import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Id;
import aldor.ui.AldorIcons;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import javax.swing.JTree;
import java.util.Comparator;
import java.util.Map;

@SuppressWarnings("serial")
public class AldorTypeHierarchyBrowser extends TypeHierarchyBrowserBase {
    private static final Logger LOG = Logger.getInstance(AldorTypeHierarchyBrowser.class);

    public AldorTypeHierarchyBrowser(Project project, PsiElement element) {
        super(project, element);
    }

    @Override
    protected boolean isInterface(PsiElement psiElement) {
        return false;
    }

    @Override
    protected boolean canBeDeleted(PsiElement psiElement) {
        return false;
    }

    @Override
    protected String getQualifiedName(PsiElement psiElement) {
        // Called on deletion - no other callers found
        throw new UnsupportedOperationException("getQualName: " + psiElement);
    }

    @Nullable
    @Override
    protected PsiElement getElementFromDescriptor(@NotNull HierarchyNodeDescriptor descriptor) {
        return descriptor.getPsiElement();
    }

    @Override
    protected void createTrees(@NotNull Map<String, JTree> trees) {
        createTreeAndSetupCommonActions(trees, IdeActions.GROUP_TYPE_HIERARCHY_POPUP);
    }

    @Nullable
    @Override
    protected JPanel createLegendPanel() {
        return null;
    }


    @Override
    protected void prependActions(@NotNull DefaultActionGroup actionGroup) {
        actionGroup.add(new AldorTypeHierarchyBrowser.ShowOperationsAction());
    }


    @Override
    protected boolean isApplicableElement(@NotNull PsiElement element) {
        Syntax syntax = SyntaxPsiParser.parse(element);
        if (syntax == null) {
            return false;
        }
        return syntax.is(Apply.class) || syntax.is(Id.class);
    }

    @Override
    @Nullable
    protected HierarchyTreeStructure createHierarchyTreeStructure(@NotNull final String typeName, @NotNull final PsiElement psiElement) {
        if (SUPERTYPES_HIERARCHY_TYPE.equals(typeName)) {
            return AldorParentCategoryHierarchyTreeStructure.createRootTreeStructure(myProject, psiElement);
        }
        else {
            return new NullHierarchyTreeStructure(psiElement, "No '" + typeName + "' structure - not implemented" );
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    protected Comparator<NodeDescriptor> getComparator() {
        return Comparator.comparing(NodeDescriptor::toString);
    }

    @SuppressWarnings("InnerClassMayBeStatic") // Should have some per-instance state, eventually
    protected class ShowOperationsAction extends ToggleAction {
        public ShowOperationsAction() {
            super("Show operations", "Show operations", AldorIcons.OPERATION);
        }

        @Override
        public final boolean isSelected(final AnActionEvent event) {
            return true;
        }

        @Override
        public final void setSelected(final AnActionEvent event, final boolean flag) {
        }

        @Override
        public final void update(@NotNull final AnActionEvent event) {
            super.update(event);
        }
    }


}
