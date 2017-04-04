package aldor.editor.finder;

import aldor.parser.NavigatorFactory;
import aldor.psi.SpadAbbrev;
import aldor.psi.index.AbbrevAbbrevIndex;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.ProjectScope;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SpadAbbrevGotoClassContributor implements ChooseByNameContributor {

    @NotNull
    @Override
    public String[] getNames(Project project, boolean nonProjectItems) {
        Collection<String> keys = AbbrevAbbrevIndex.instance.getAllKeys(project);
        return keys.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean nonProjectItems) {
        GlobalSearchScope scope = GlobalSearchScopesCore.projectProductionScope(project);
        if (nonProjectItems) {
            scope = GlobalSearchScope.union(new GlobalSearchScope[] {scope, ProjectScope.getLibrariesScope(project)});
        }
        Collection<SpadAbbrev> items = AbbrevAbbrevIndex.instance.get(name, project, scope);
        List<NavigationItem> collect = items.stream()
                .filter(abbrev -> !abbrev.abbrevInfo().isError())
                .map(abbrev -> NavigatorFactory.get(project).getNavigationItem(abbrev))
                .collect(Collectors.toList());
        return collect.toArray(NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY);

    }
}
