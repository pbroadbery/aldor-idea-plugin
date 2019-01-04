package aldor.hierarchy;

import aldor.hierarchy.AldorGroupedHierarchyTreeStructure.Grouping;
import aldor.hierarchy.AldorGroupedHierarchyTreeStructure.GroupingKey;
import aldor.hierarchy.util.ComparatorPriority;
import aldor.syntax.SyntaxPrinter;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.awt.Font;

public class GroupingHierarchyDescriptor extends HierarchyNodeDescriptor implements ComparatorPriority {
    private final Grouping grouping;

    public GroupingHierarchyDescriptor(AldorHierarchyNodeDescriptor parent, @NotNull PsiElement baseElement, Grouping grouping) {
        super(baseElement.getProject(), parent, new GroupedPsiElement(baseElement, grouping), false);
        this.grouping = grouping;
    }

    @Override
    public boolean update() {
        CompositeAppearance oldText = myHighlightedText;

        boolean changes = super.update();

        GroupingKey key = grouping.key();
        myHighlightedText = new CompositeAppearance();
        TextAttributes mainTextAttributes = new TextAttributes(myColor, null, null, null, Font.PLAIN);

        myHighlightedText.getBeginning().addText(key.name() + ": " + SyntaxPrinter.instance().toString(key.type()), mainTextAttributes);
        myName = myHighlightedText.getText();

        if (!Comparing.equal(myHighlightedText, oldText)) {
            changes = true;
        }

        return changes;
    }

    @Override
    public int priority() {
        return 1;
    }
}
