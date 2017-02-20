package aldor.parser;

import aldor.psi.AldorDefineStubbing;
import aldor.psi.AldorIdentifier;
import aldor.psi.SpadAbbrevStubbing;
import com.intellij.navigation.NavigationItem;

/**
 * Mapping between PSI items and NavigationItem.
 */
public interface Navigator {
    NavigationItem getNavigationItem(SpadAbbrevStubbing.SpadAbbrev ident);

    NavigationItem getNavigationItem(AldorIdentifier ident);

    NavigationItem getNavigationItem(AldorDefineStubbing.AldorDefine define);
}
