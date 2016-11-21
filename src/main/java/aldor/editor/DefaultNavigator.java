package aldor.editor;

import aldor.parser.NavigatorFactory;
import aldor.psi.AldorIdentifier;
import com.intellij.navigation.NavigationItem;

public class DefaultNavigator implements NavigatorFactory.Navigator {

    @Override
    public NavigationItem getNavigationItem(AldorIdentifier ident) {
        return new AldorIdentifierNavigationItem(ident);
    }
}
