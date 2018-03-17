package aldor.hierarchy;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class NullHierarchyTreeStructure extends HierarchyTreeStructure {
    public static final Object[] EMPTY_OBJECTS = new Object[0];
    private final String msg;

    public NullHierarchyTreeStructure(PsiElement psiElement, String s) {
        super(psiElement.getProject(), new NullHierarchyDescriptor(psiElement));
        this.msg = s;
    }

    @NotNull
    @Override
    protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
        return new Object[] {createDescriptor("Error: " + msg, descriptor)};
    }

    private static final class NullHierarchyDescriptor extends HierarchyNodeDescriptor {
        private NullHierarchyDescriptor(PsiElement psiElement) {
            super(psiElement.getProject(), null, psiElement, true);
        }

        @Override
        public boolean update() {
            CompositeAppearance text = new CompositeAppearance();

            boolean changes = super.update();

            final PsiElement enclosingElement = getPsiElement();
            if (enclosingElement == null) {
                final String invalidPrefix = IdeBundle.message("node.hierarchy.invalid");
                if (!myHighlightedText.getText().startsWith(invalidPrefix)) {
                    myHighlightedText.getBeginning().addText(invalidPrefix, HierarchyNodeDescriptor.getInvalidPrefixAttributes());
                }
                return true;
            }
            text.getBeginning().addText(enclosingElement.getText(), (TextAttributes) null);
            if (myHighlightedText.equals(text)) {
                return changes;
            }
            this.myHighlightedText = text;
            this.myName = enclosingElement.getText();
            return true;
        }

    }
}
