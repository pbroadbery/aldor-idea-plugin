package aldor.hierarchy;

import aldor.ui.AldorIcons;

public class ViewFlatHierarchyAction extends AbstractChangeHierarchyViewAction {
    ViewFlatHierarchyAction() {
        super("Flat View", "View flattened hierarchy", AldorIcons.OPERATION); // TODO: Wrong icon
    }

    @Override
    protected String getTypeName() {
        return AldorTypeHierarchyConstants.FLAT_HIERARCHY_TYPE;
    }
}
