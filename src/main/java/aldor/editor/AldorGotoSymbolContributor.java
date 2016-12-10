package aldor.editor;

import aldor.psi.index.AldorDefineNameIndex;

public class AldorGotoSymbolContributor extends AldorGotoDefinitionContributorBase {

    protected AldorGotoSymbolContributor() {
        super(AldorDefineNameIndex.instance);
    }
}
