package aldor.hierarchy;

import aldor.ui.AldorIcons;

public class ViewGroupedHierarchyAction extends AbstractChangeHierarchyViewAction {
    ViewGroupedHierarchyAction() {
        super("Grouped View", "View grouped hierarchy", AldorIcons.OPERATION); // TODO: Wrong icon
    }

    @Override
    protected String getTypeName() {
        return AldorTypeHierarchyConstants.GROUPED_HIERARCHY_TYPE;
    }
}
