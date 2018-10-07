package aldor.hierarchy;

import aldor.ui.AldorIcons;

import static com.intellij.ide.hierarchy.TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE;

public class ViewParentHierarchyAction extends AbstractChangeHierarchyViewAction {
    ViewParentHierarchyAction() {
        super("Parent View", "View hierarchy", AldorIcons.OPERATION); // TODO: Wrong icon
    }
    @Override
    protected String getTypeName() {
        return SUPERTYPES_HIERARCHY_TYPE;
    }
}
