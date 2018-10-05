package aldor.hierarchy;

import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.psi.PsiElement;
import org.junit.Assert;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.ide.hierarchy.TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE;

class TestBrowser implements Disposable {
    @Nonnull
    public final AldorTypeHierarchyBrowser browser;
    @Nonnull
    public final HierarchyTreeStructure hierarchy;

    TestBrowser(AldorTypeHierarchyProvider provider, PsiElement element, String hierarchyType) {
        DataContext context = SimpleDataContext.getSimpleContext(CommonDataKeys.PSI_ELEMENT.getName(), element,
                SimpleDataContext.getProjectContext(element.getProject()));
        PsiElement target = provider.getTarget(context);
        Assert.assertNotNull(target);
        browser = (AldorTypeHierarchyBrowser) provider.createHierarchyBrowser(target);
        provider.browserActivated(browser);
        HierarchyTreeStructure tmp = browser.createHierarchyTreeStructure(SUPERTYPES_HIERARCHY_TYPE, target);
        Assert.assertNotNull(tmp);
        hierarchy = tmp;
    }

    AldorHierarchyNodeDescriptor rootDescriptor() {
        return (AldorHierarchyNodeDescriptor) hierarchy.getRootElement();
    }

    public List<NodeDescriptor<?>> childElements() {
        return Arrays.stream(hierarchy.getChildElements(hierarchy.getRootElement()))
                .map(e -> (NodeDescriptor<?>) e)
                .collect(Collectors.toList());
    }

    public void update() {
        rootDescriptor().update();
        childElements().forEach(NodeDescriptor::update);
    }

    @Override
    public void dispose() {
        browser.dispose();
    }
}
