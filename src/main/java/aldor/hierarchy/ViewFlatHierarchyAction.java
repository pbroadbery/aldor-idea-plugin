package aldor.hierarchy;

import aldor.ui.AldorIcons;

import static aldor.hierarchy.AldorTypeHierarchyBrowser.FLAT_HIERARCHY_TYPE;

@SuppressWarnings("ComponentNotRegistered")
public class ViewFlatHierarchyAction extends AbstractChangeHierarchyViewAction {
    ViewFlatHierarchyAction() {
        super("Flat View", "View flattened hierarchy", AldorIcons.OPERATION); // TODO: Wrong icon
    }

    @Override
    protected String getTypeName() {
        return FLAT_HIERARCHY_TYPE;
    }
}
