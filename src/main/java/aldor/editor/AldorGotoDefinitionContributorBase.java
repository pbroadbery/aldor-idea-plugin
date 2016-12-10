package aldor.editor;

import aldor.parser.NavigatorFactory;
import aldor.psi.AldorDefineStubbing.AldorDefine;
import aldor.psi.AldorIdentifier;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AldorGotoDefinitionContributorBase implements ChooseByNameContributor {
    private final StringStubIndexExtension<AldorDefine> index;

    protected AldorGotoDefinitionContributorBase(StringStubIndexExtension<AldorDefine> index) {
        this.index = index;
    }

    @NotNull
    @Override
    public String[] getNames(Project project, boolean nonProjectItems) {
        Collection<String> keys = index.getAllKeys(project);
        return keys.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean nonProjectItems) {
        Collection<AldorDefine> items = index.get(name, project, GlobalSearchScope.allScope(project));
        List<NavigationItem> collect = items.stream()
                .map(define -> navigationItemForIndexEntry(project, define))
                .flatMap(identMaybe -> identMaybe.map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());
        return collect.toArray(NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY);

    }

    Optional<NavigationItem> navigationItemForIndexEntry(Project project, AldorDefine define) {
        Optional<AldorIdentifier> identMaybe = define.defineIdentifier();

        return identMaybe.map(ident -> NavigatorFactory.get(project).getNavigationItem(ident));
    }

}
