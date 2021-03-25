package aldor.structure;

import aldor.psi.AldorDefine;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import icons.AldorIcons;
import org.jetbrains.annotations.NotNull;

public class MacroFilter implements Filter {
    static final String ID = "MACRO_FILTER";
    @Override
    public boolean isVisible(TreeElement treeNode) {
        if (!(treeNode instanceof DefineTreeElement)) {
            return true;
        }
        DefineTreeElement elt = (DefineTreeElement) treeNode;
        if (elt.getElement() == null) {
            return true;
        }
        return elt.getElement().definitionType() != AldorDefine.DefinitionType.MACRO;
    }

    @Override
    public boolean isReverted() {
        return true;
    }

    @NotNull
    @Override
    public ActionPresentation getPresentation() {
        return new ActionPresentationData("Show Macros", "show macro definitions", AldorIcons.MACRO);
    }

    @NotNull
    @Override
    public String getName() {
        return ID;
    }
}
