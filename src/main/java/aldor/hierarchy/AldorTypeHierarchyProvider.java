package aldor.hierarchy;

import aldor.psi.AldorIdentifier;
import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.ide.hierarchy.TypeHierarchyBrowserBase.SUPERTYPES_HIERARCHY_TYPE;

public class AldorTypeHierarchyProvider implements HierarchyProvider {
    private static final Logger LOG = Logger.getInstance(AldorTypeHierarchyProvider.class);

    @Nullable
    @Override
    public PsiElement getTarget(@NotNull DataContext dataContext) {
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return null;
        }

        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

        if (editor != null) {
            final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if (file == null) {
                return null;
            }

            return PsiTreeUtil.findElementOfClassAtOffset(file, editor.getCaretModel().getOffset(), AldorIdentifier.class, false);
        }

        PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (element == null) {
            return null;
        }

        return element;

    }

    @NotNull
    @Override
    public HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement target) {
        return new AldorTypeHierarchyBrowser(target.getProject(), target);
    }

    @Override
    public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {
        ((HierarchyBrowserBaseEx) hierarchyBrowser).changeView(SUPERTYPES_HIERARCHY_TYPE, true);
    }
}
