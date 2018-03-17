package aldor.editor;

import com.intellij.openapi.project.Project;

public interface ProjectPsiDefaults {
    PsiElementToLookupElementMapping factory();

    static PsiElementToLookupElementMapping lookupElementFactory(Project project) {
        return project.getComponent(ProjectPsiDefaults.class).factory();
    }

}
