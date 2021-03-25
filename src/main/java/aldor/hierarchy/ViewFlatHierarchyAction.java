package aldor.hierarchy;

import icons.AldorIcons;

public class ViewFlatHierarchyAction extends AbstractChangeHierarchyViewAction {
    ViewFlatHierarchyAction() {
        super("Flat View", "View flattened hierarchy", AldorIcons.ParentHierarchyView); // TODO: Wrong icon
    }

    @Override
    protected String getTypeName() {
        return AldorTypeHierarchyConstants.FLAT_HIERARCHY_TYPE;
    }
}
