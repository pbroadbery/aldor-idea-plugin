package aldor.hierarchy;

import aldor.hierarchy.util.ComparatorPriority;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.ui.AldorIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.Font;

public class AldorHierarchyNodeDescriptor extends HierarchyNodeDescriptor implements ComparatorPriority {
    private static final Logger LOG = Logger.getInstance(AldorHierarchyNodeDescriptor.class);
    private final Syntax syntax;

    protected AldorHierarchyNodeDescriptor(@NotNull Project project, NodeDescriptor<PsiElement> parentDescriptor, @NotNull PsiElement element, Syntax syntax, boolean isBase) {
        super(project, parentDescriptor, element, isBase);
        this.syntax = syntax;
    }

    public Syntax syntax() {
        return syntax;
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
        TextAttributes mainTextAttributes = null;
        if (myColor != null) {
            mainTextAttributes = new TextAttributes(myColor, null, null, null, Font.PLAIN);
        }

        myHighlightedText.getBeginning().addText(SyntaxPrinter.instance().toString(syntax), mainTextAttributes);
        myName = myHighlightedText.getText();

        if (!Comparing.equal(myHighlightedText, oldText)) {
            changes = true;
        }

        return changes;
    }

    @Nullable
    @Override
    protected Icon getIcon(@NotNull PsiElement element) {
        return AldorIcons.TYPE;
    }

    @Override
    public int priority() {
        return 10;
    }
}
