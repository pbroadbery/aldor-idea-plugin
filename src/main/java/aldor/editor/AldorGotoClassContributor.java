package aldor.editor;

import aldor.psi.index.AldorDefineTopLevelIndex;

public class AldorGotoClassContributor extends AldorGotoDefinitionContributorBase {

    protected AldorGotoClassContributor() {
        super(AldorDefineTopLevelIndex.instance);
    }
}
