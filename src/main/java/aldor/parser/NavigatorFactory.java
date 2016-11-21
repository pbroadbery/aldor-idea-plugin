package aldor.parser;

import aldor.psi.AldorIdentifier;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;

public final class NavigatorFactory {
    public static final NavigatorFactory instance = new NavigatorFactory();
    private Navigator defaultNavigator;

    private NavigatorFactory() {
        setDefaultNavigator(null);
    }

    public static void registerDefaultNavigator(Navigator navigator) {
        instance.setDefaultNavigator(navigator);
    }

    public static Navigator get(Project project) {
        return instance.getDefaultNavigator();
    }

    public Navigator getDefaultNavigator() {
        return defaultNavigator;
    }

    public void setDefaultNavigator(Navigator defaultNavigator) {
        this.defaultNavigator = defaultNavigator;
    }

    public interface Navigator {
        NavigationItem getNavigationItem(AldorIdentifier ident);
    }
}
