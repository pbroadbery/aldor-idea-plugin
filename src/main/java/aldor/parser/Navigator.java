package aldor.parser;

import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.SpadAbbrev;
import com.intellij.navigation.NavigationItem;

/**
 * Mapping between PSI items and NavigationItem.
 */
public interface Navigator {
    NavigationItem getNavigationItem(SpadAbbrev ident);

    NavigationItem getNavigationItem(AldorIdentifier ident);

    NavigationItem getNavigationItem(AldorDefine define);
}
