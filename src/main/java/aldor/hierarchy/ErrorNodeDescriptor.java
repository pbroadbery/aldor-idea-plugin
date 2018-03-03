package aldor.hierarchy;

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import org.jetbrains.annotations.NotNull;

import static aldor.util.Assertions.isNotNull;

public class ErrorNodeDescriptor extends HierarchyNodeDescriptor implements ComparatorPriority {
    private final String text;

    public ErrorNodeDescriptor(@NotNull AldorHierarchyNodeDescriptor parent, String s) {
        super(isNotNull(parent.getProject()), parent, isNotNull(parent.getPsiElement()), false);
        this.text = s;
    }

    @Override
    public boolean update() {
        boolean changes = super.update();
        CompositeAppearance appearance = new CompositeAppearance();
        appearance.getBeginning().addText(text);
        if (!this.myName.equals(text)) {
            this.myName = text;
            this.myHighlightedText = appearance;
            changes = true;
        }
        return changes;
    }

    @Override
    public int priority() {
        return 1;
    }
}
