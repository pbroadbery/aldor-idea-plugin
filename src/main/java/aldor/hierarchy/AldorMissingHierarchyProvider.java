package aldor.hierarchy;

import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class AldorMissingHierarchyProvider implements HierarchyProvider {
    @Nullable
    @Override
    public PsiElement getTarget(@NotNull DataContext dataContext) {
        return null;
    }

    @NotNull
    @Override
    public HierarchyBrowser createHierarchyBrowser(PsiElement target) {
        return new MissingHierarchyBrowser();
    }

    @Override
    public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {

    }

    private static class MissingHierarchyBrowser implements HierarchyBrowser {

        @Override
        public JComponent getComponent() {
            return new JLabel("Missing");
        }

        @Override
        public void setContent(Content content) {

        }
    }
}
