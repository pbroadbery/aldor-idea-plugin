package aldor.editor;

import aldor.parser.NavigatorFactory;
import aldor.psi.AldorIdentifier;
import com.intellij.navigation.NavigationItem;

/**
 * Created by pab on 19/11/16.
 */
public class DefaultNavigator implements NavigatorFactory.Navigator {

    @Override
    public NavigationItem getNavigationItem(AldorIdentifier ident) {
        return new AldorIdentifierNavigationItem(ident);
    }
}
