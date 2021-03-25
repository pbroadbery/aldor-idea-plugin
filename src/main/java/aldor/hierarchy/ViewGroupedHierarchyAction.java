package aldor.hierarchy;

import icons.AldorIcons;

public class ViewGroupedHierarchyAction extends AbstractChangeHierarchyViewAction {
    ViewGroupedHierarchyAction() {
        super("Grouped View", "View grouped hierarchy", AldorIcons.GroupedHierarchyView); // TODO: Wrong icon
    }

    @Override
    protected String getTypeName() {
        return AldorTypeHierarchyConstants.GROUPED_HIERARCHY_TYPE;
    }
}
