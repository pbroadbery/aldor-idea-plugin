package aldor.hierarchy;

import aldor.syntax.SyntaxPrinter;
import aldor.ui.AldorIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.Font;

import static aldor.spad.SpadLibrary.Operation;

public class AldorHierarchyOperationDescriptor  extends HierarchyNodeDescriptor implements ComparatorPriority {
    private final Operation operation;

    protected AldorHierarchyOperationDescriptor(@NotNull Project project, HierarchyNodeDescriptor parentDescriptor, Operation operation) {
        super(project, parentDescriptor, anchorElement(parentDescriptor, operation), false);
        this.operation = operation;
    }

    private static PsiElement anchorElement(HierarchyNodeDescriptor parentDescriptor, Operation operation) {
        if (operation.declaration() != null) {
            return operation.declaration();
        } else if (operation.containingForm() != null) {
            return operation.containingForm();
        }
        else {
            return parentDescriptor.getPsiElement();
        }
    }

    public Operation operation() {
        return operation;
    }

    @Override
    public final boolean update() {
        final CompositeAppearance oldText = myHighlightedText;

        final PsiElement enclosingElement = getPsiElement();
        if (enclosingElement == null) {
            final String invalidPrefix = IdeBundle.message("node.hierarchy.invalid");
            if (!myHighlightedText.getText().startsWith(invalidPrefix)) {
                myHighlightedText.getBeginning().addText(invalidPrefix, HierarchyNodeDescriptor.getInvalidPrefixAttributes());
            }
            return true;
        }

        boolean changes = super.update();

        myHighlightedText = new CompositeAppearance();
        int fontStyle = (this.operation.declaration() == null) ? Font.ITALIC : Font.PLAIN;
        TextAttributes mainTextAttributes = new TextAttributes(myColor, null, null, null, fontStyle);

        myHighlightedText.getBeginning().addText(operation.name() + ": " + SyntaxPrinter.instance().toString(operation.type()), mainTextAttributes);
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

    @Nullable
    @Override
    protected Icon getIcon(@NotNull PsiElement element) {
        return AldorIcons.IDENTIFIER;
    }
}
