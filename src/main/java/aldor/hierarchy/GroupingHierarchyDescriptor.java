package aldor.hierarchy;

import aldor.hierarchy.util.ComparatorPriority;
import aldor.spad.SpadLibrary;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxUtils;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.awt.Font;
import java.util.List;

// TODO: Lets sort out hierarchy browsers
public class GroupingHierarchyDescriptor extends HierarchyNodeDescriptor implements ComparatorPriority {
    private final Grouping grouping;

    public GroupingHierarchyDescriptor(AldorHierarchyNodeDescriptor parent, @NotNull PsiElement baseElement, GroupingHierarchyDescriptor.Grouping grouping) {
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


    GroupingKey groupingKey(SpadLibrary.Operation op) {
        return new GroupingKey(op.name(), op.type());
    }

    public static class GroupingKey {
        private final String name;
        private final Syntax type;

        GroupingKey(String name, Syntax type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != getClass()) {
                return false;
            }
            GroupingKey other = (GroupingKey) obj;

            return name.equals(other.name()) && SyntaxUtils.match(other.type(), type);
        }

        public String name() {
            return name;
        }

        public Syntax type() {
            return type;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    static class Grouping {
        private final GroupingKey key;
        private final List<SpadLibrary.Operation> operations;

        Grouping(GroupingKey type, List<SpadLibrary.Operation> operations) {
            this.key = type;
            this.operations = operations;
        }

        public GroupingKey key() {
            return key;
        }

        public List<SpadLibrary.Operation> operations() {
            return operations;
        }
    }

}
