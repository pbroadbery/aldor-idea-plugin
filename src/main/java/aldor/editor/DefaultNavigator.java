package aldor.editor;

import aldor.parser.Navigator;
import aldor.psi.AldorIdentifier;
import aldor.psi.SpadAbbrevStubbing;
import com.intellij.navigation.NavigationItem;

public class DefaultNavigator implements Navigator {

    @Override
    public NavigationItem getNavigationItem(SpadAbbrevStubbing.SpadAbbrev abbrev) {
        return new SpadAbbrevNavigationItem(abbrev);
    }

    @Override
    public NavigationItem getNavigationItem(AldorIdentifier ident) {
        return new AldorIdentifierNavigationItem(ident);
    }
}
