package aldor.editor;

import aldor.parser.NavigatorFactory;
import aldor.psi.AldorDefine;
import aldor.psi.index.AldorDefineNameIndex;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AldorGotoClassContributor implements ChooseByNameContributor {

    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        Collection<String> keys = AldorDefineNameIndex.instance.getAllKeys(project);
        return keys.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        Collection<AldorDefine> items = AldorDefineNameIndex.instance.get(name, project, GlobalSearchScope.allScope(project));
        List<NavigationItem> collect = items.stream()
                .map(AldorDefine::defineIdentifier)
                .map(identMaybe-> identMaybe.map(ident2 -> NavigatorFactory.get(project).getNavigationItem(ident2)))
                .flatMap(identMaybe -> identMaybe.map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());
        return collect.toArray(NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY);

    }
}
