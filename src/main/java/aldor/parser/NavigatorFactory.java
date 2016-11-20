package aldor.parser;

import aldor.psi.AldorIdentifier;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;

public final class NavigatorFactory {
    public static final NavigatorFactory instance = new NavigatorFactory();
    private Navigator defaultNavigator;

    private NavigatorFactory() {
        defaultNavigator = null;
    }

    public static void registerDefaultNavigator(Navigator navigator) {
        instance.defaultNavigator = navigator;
    }

    public static Navigator get(Project project) {
        return instance.defaultNavigator;
    }

    public interface Navigator {
        NavigationItem getNavigationItem(AldorIdentifier ident);
    }
}
