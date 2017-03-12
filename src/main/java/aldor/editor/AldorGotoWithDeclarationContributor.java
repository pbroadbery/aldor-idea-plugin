package aldor.editor;

import aldor.parser.NavigatorFactory;
import aldor.psi.AldorDeclare;
import aldor.psi.index.AldorDeclareTopIndex;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AldorGotoWithDeclarationContributor implements ChooseByNameContributor {
    private final StringStubIndexExtension<AldorDeclare> index = AldorDeclareTopIndex.instance;

    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        Collection<String> keys = index.getAllKeys(project);
        return keys.toArray(ArrayUtil.EMPTY_STRING_ARRAY);

    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        Collection<AldorDeclare> items = index.get(name, project, GlobalSearchScope.allScope(project));
        List<NavigationItem> collect = items.stream()
                .map(declare -> navigationItemForIndexEntry(project, declare))
                .collect(Collectors.toList());
        return collect.toArray(NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY);
    }

    private NavigationItem navigationItemForIndexEntry(Project project, AldorDeclare declare) {
        return NavigatorFactory.get(project).getNavigationItem(declare);
    }
}
