package aldor.hierarchy;

import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Id;
import aldor.ui.AldorActions;
import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.PopupHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import javax.swing.JTree;
import java.util.Comparator;
import java.util.Map;

import static aldor.syntax.SyntaxPsiParser.SurroundType.Leading;
import static aldor.syntax.SyntaxUtils.psiElementFromSyntax;

@SuppressWarnings("serial")
public class AldorTypeHierarchyBrowser extends TypeHierarchyBrowserBase {
    private static final Logger LOG = Logger.getInstance(AldorTypeHierarchyBrowser.class);
    public static final String FLAT_HIERARCHY_TYPE = "Categories and Operations";

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
        createTreeAndSetupCommonActions(trees, AldorActions.GROUP_TYPE_HIERARCHY_POPUP);
    }

    @Override
    protected void createTreeAndSetupCommonActions(@NotNull Map<String, JTree> trees, ActionGroup group) {
        super.createTreeAndSetupCommonActions(trees, group);
        final BaseOnThisTypeAction baseOnThisTypeAction = createBaseOnThisAction();
        final JTree tree1 = createTree(true);
        PopupHandler.installPopupHandler(tree1, group, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP, ActionManager.getInstance());
        baseOnThisTypeAction
                .registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_TYPE_HIERARCHY).getShortcutSet(), tree1);
        trees.put(FLAT_HIERARCHY_TYPE, tree1);
    }

    @Override
    @NotNull
    protected BaseOnThisTypeAction createBaseOnThisAction() {
        return new AldorBaseOnThisTypeAction();
    }


    @Nullable
    @Override
    protected JPanel createLegendPanel() {
        return null;
    }


    @Override
    protected void prependActions(@NotNull DefaultActionGroup actionGroup) {
        actionGroup.add(new ViewParentHierarchyAction());
        actionGroup.add(new ViewFlatHierarchyAction());
        actionGroup.add(new AlphaSortAction());

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
    public HierarchyTreeStructure createHierarchyTreeStructure(@NotNull final String typeName, @NotNull final PsiElement element) {
        Syntax syntax = SyntaxPsiParser.surroundingApplication(element, Leading);
        if (syntax == null) {
            return new NullHierarchyTreeStructure(element, "Invalid element - " + element);
        }
        syntax = SyntaxUtils.typeName(syntax);
        if (psiElementFromSyntax(syntax) == null) {
            return new NullHierarchyTreeStructure(element, "Failed to find syntax form for " + element.getText());
        }

        if (SUPERTYPES_HIERARCHY_TYPE.equals(typeName)) {
            return new AldorParentCategoryHierarchyTreeStructure(myProject, syntax);
        }
        else if (FLAT_HIERARCHY_TYPE.equals(typeName)) {
            return new AldorFlatHierarchyTreeStructure(myProject, syntax);
        }
        else {
            return new NullHierarchyTreeStructure(element, "No '" + typeName + "' structure - not implemented" );
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    protected Comparator<NodeDescriptor> getComparator() {
        return Comparator
                .comparing(this::descriptorPriority)
                .reversed().<NodeDescriptor, Integer>thenComparing(Comparator.comparing(NodeDescriptor::toString));
    }

    @SuppressWarnings("rawtypes")
    private Integer descriptorPriority(NodeDescriptor descriptor) {
        if (descriptor instanceof ComparatorPriority) {
            return ((ComparatorPriority) descriptor).priority();
        }
        return ComparatorPriority.UNKNOWN;
    }

    public String typeName() {
        return this.getCurrentViewType();
    }

    public static class AldorBaseOnThisTypeAction extends BaseOnThisTypeAction {

        public AldorBaseOnThisTypeAction() {
        }

        @Override
        protected boolean isEnabled(@NotNull HierarchyBrowserBaseEx browser, @NotNull PsiElement element) {
            return false;
        }
    }
}
