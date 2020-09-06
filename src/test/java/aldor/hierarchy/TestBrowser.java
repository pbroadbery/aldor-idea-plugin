package aldor.hierarchy;

import aldor.test_util.CloseCheck;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import org.junit.Assert;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class TestBrowser implements Disposable, AutoCloseable {
    private static final Logger LOG = Logger.getInstance(TestBrowser.class);

    @Nonnull
    public final AldorTypeHierarchyBrowser browser;
    @Nonnull
    public final HierarchyTreeStructure hierarchy;
    @Nonnull
    private final CloseCheck closeCheck;

    TestBrowser(CloseCheck check, AldorTypeHierarchyProvider provider, PsiElement element, String hierarchyType) {
        DataContext context = SimpleDataContext.getSimpleContext(CommonDataKeys.PSI_ELEMENT.getName(), element,
                SimpleDataContext.getProjectContext(element.getProject()));
        PsiElement target = provider.getTarget(context);
        Assert.assertNotNull(target);
        browser = (AldorTypeHierarchyBrowser) provider.createHierarchyBrowser(target);
        provider.browserActivated(browser);
        HierarchyTreeStructure tmp = browser.createHierarchyTreeStructure(hierarchyType, target);
        Assert.assertNotNull(tmp);
        hierarchy = tmp;
        closeCheck = check;
    }

    AldorHierarchyNodeDescriptor rootDescriptor() {
        return (AldorHierarchyNodeDescriptor) hierarchy.getRootElement();
    }

    public List<NodeDescriptor<?>> childElements() {
        return Arrays.stream(hierarchy.getChildElements(hierarchy.getRootElement()))
                .peek(e -> LOG.info("Child: " + e))
                .map(e -> (NodeDescriptor<?>) e)
                .collect(Collectors.toList());
    }

    public void update() {
        rootDescriptor().update();
        childElements().forEach(NodeDescriptor::update);
    }

    @Override
    public void dispose() {
        closeCheck.close(this);
        browser.dispose();
    }

    @Override
    public void close() {
        this.dispose();
    }
}
