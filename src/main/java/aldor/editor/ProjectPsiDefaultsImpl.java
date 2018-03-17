package aldor.editor;

import com.intellij.openapi.project.Project;

/**
 * Project level defaults for PSI related things.
 * This is mostly to avoid a long dependency chain from Aldor PsiElements to Icons and so on.
 */
public class ProjectPsiDefaultsImpl implements ProjectPsiDefaults {
    private final PsiElementToLookupElementMapping factory = new AldorLookupElementFactory();

    ProjectPsiDefaultsImpl(Project project) {
    }

    @Override
    public PsiElementToLookupElementMapping factory() {
        return factory;
    }
}
