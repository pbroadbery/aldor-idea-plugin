package aldor.hierarchy;

import aldor.hierarchy.AldorGroupedHierarchyTreeStructure.Grouping;
import aldor.syntax.SyntaxPrinter;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class GroupedPsiElement extends FakePsiElement {
    private final PsiElement parent;
    private final Grouping grouping;

    GroupedPsiElement(PsiElement parent, Grouping grouping) {
        this.parent = parent;
        this.grouping = grouping;
    }
    @Override
    public PsiElement getParent() {
        return parent;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                AldorGroupedHierarchyTreeStructure.GroupingKey key = grouping.key();
                // Maybe should wrap a declare aroung the type, then convert
                return key.name() + ": " + SyntaxPrinter.instance().toString(key.type());
            }

            @Nullable
            @Override
            public String getLocationString() {
                return grouping.operations().size() + " places";
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return null;
            }
        };
    }


}
