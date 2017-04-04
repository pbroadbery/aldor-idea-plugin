package aldor.editor.finder;

import aldor.psi.index.AldorDefineTopLevelIndex;

public class AldorGotoClassContributor extends AldorGotoDefinitionContributorBase {

    public AldorGotoClassContributor() {
        super(AldorDefineTopLevelIndex.instance);
    }
}
