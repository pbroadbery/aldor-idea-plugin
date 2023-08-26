package aldor.editor.finder;

import aldor.parser.NavigatorFactory;
import aldor.psi.AldorDefine;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.indexing.FindSymbolParameters;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class AldorGotoDefinitionContributorBase implements ChooseByNameContributor {
    private final StringStubIndexExtension<AldorDefine> index;

    protected AldorGotoDefinitionContributorBase(StringStubIndexExtension<AldorDefine> index) {
        this.index = index;
    }

    @NotNull
    @Override
    public String[] getNames(Project project,
                             @SuppressWarnings("NegativelyNamedBooleanVariable") boolean nonProjectItems) {
        Collection<String> result = new ArrayList<>();
        var scope = GlobalSearchScope.allScope(project);
        index.processAllKeys(project, k -> {
            if (!index.get(k, project, scope).isEmpty()) {
                result.add(k);
            }
            return true;
        });
        return ArrayUtilRt.toStringArray(result);
    }


    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project,
                                           @SuppressWarnings("NegativelyNamedBooleanVariable") boolean nonProjectItems) {
        GlobalSearchScope scope = GlobalSearchScopesCore.projectProductionScope(project);
        if (nonProjectItems) {
            scope = GlobalSearchScope.union(new GlobalSearchScope[] {scope, ProjectScope.getLibrariesScope(project)});
        }

        Collection<AldorDefine> items = index.get(name, project, scope);
        List<NavigationItem> collect = items.stream()
                .map(define -> navigationItemForIndexEntry(project, define))
                .collect(Collectors.toList());
        return collect.toArray(NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY);
    }

    NavigationItem navigationItemForIndexEntry(Project project, AldorDefine define) {
        return NavigatorFactory.get(project).getNavigationItem(define);
    }

}
