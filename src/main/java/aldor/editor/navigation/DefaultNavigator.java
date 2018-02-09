package aldor.editor.navigation;

import aldor.parser.Navigator;
import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.SpadAbbrev;
import com.intellij.navigation.NavigationItem;

public class DefaultNavigator implements Navigator {

    @Override
    public NavigationItem getNavigationItem(SpadAbbrev abbrev) {
        return new SpadAbbrevNavigationItem(abbrev);
    }

    @Override
    public NavigationItem getNavigationItem(AldorIdentifier ident) {
        return new AldorIdentifierNavigationItem(ident);
    }

    @Override
    public NavigationItem getNavigationItem(AldorDefine define) {
        return new AldorDefineNavigationItem(define);
    }

    @Override
    public NavigationItem getNavigationItem(AldorDeclare declare) {
        return new AldorDeclareNavigationItem(declare);
    }
}
