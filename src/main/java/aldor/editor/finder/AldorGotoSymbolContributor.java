package aldor.editor.finder;

import aldor.psi.index.AldorDefineNameIndex;

public class AldorGotoSymbolContributor extends AldorGotoDefinitionContributorBase {

    public AldorGotoSymbolContributor() {
        super(AldorDefineNameIndex.instance);
    }
}
