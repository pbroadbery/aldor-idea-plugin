package aldor.hierarchy;

import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

@SuppressWarnings("serial")
public abstract class TypeHierarchyBrowserBaseEx extends TypeHierarchyBrowserBase {

    protected TypeHierarchyBrowserBaseEx(Project project, PsiElement element) {
        super(project, element);
    }

    public String typeName() {
        return this.getCurrentViewType();
    }

}
