package aldor.hierarchy;

import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import icons.AldorIcons;
public class ViewParentHierarchyAction extends AbstractChangeHierarchyViewAction {
    ViewParentHierarchyAction() {
        super("Parent View", "View hierarchy", AldorIcons.ParentHierarchyView); // TODO: Wrong icon
    }
    @Override
    protected String getTypeName() {
        return TypeHierarchyBrowserBase.getSupertypesHierarchyType();
    }
}
