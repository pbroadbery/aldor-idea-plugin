package aldor.hierarchy;

import com.intellij.ide.hierarchy.ChangeHierarchyViewActionBase;
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.util.ui.UIUtil;

import javax.swing.Icon;

public abstract class AbstractChangeHierarchyViewAction extends ChangeHierarchyViewActionBase {

    AbstractChangeHierarchyViewAction(final String shortDescription, final String longDescription, final Icon icon) {
        super(shortDescription, longDescription, icon);
    }

    @Override
    protected TypeHierarchyBrowserBase getHierarchyBrowser(DataContext context) {
        return UIUtil.getParentOfType(TypeHierarchyBrowserBase.class, context.getData(PlatformDataKeys.CONTEXT_COMPONENT));
    }

}
