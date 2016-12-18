package aldor.parser;

import aldor.psi.AldorIdentifier;
import aldor.psi.SpadAbbrevStubbing;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Creates navigation items for aldor Psi Elements */
public final class NavigatorFactory {
    public static final NavigatorFactory instance = new NavigatorFactory();
    @Nullable
    private Navigator defaultNavigator;

    private NavigatorFactory() {
        this.defaultNavigator = null;
    }

    public static void registerDefaultNavigator(Navigator navigator) {
        //noinspection AccessingNonPublicFieldOfAnotherObject
        instance.defaultNavigator = navigator;
    }

    public static Navigator get(Project project) {
        //noinspection AccessingNonPublicFieldOfAnotherObject
        return instance.defaultNavigator;
    }

    @NotNull
    public Navigator getDefaultNavigator() {
        assert defaultNavigator != null;
        return defaultNavigator;
    }

    public void setDefaultNavigator(@NotNull Navigator defaultNavigator) {
        this.defaultNavigator = defaultNavigator;
    }

    public interface Navigator {
        NavigationItem getNavigationItem(SpadAbbrevStubbing.SpadAbbrev ident);
        NavigationItem getNavigationItem(AldorIdentifier ident);
    }
}
