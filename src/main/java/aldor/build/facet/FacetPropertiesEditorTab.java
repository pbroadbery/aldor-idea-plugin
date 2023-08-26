package aldor.build.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class FacetPropertiesEditorTab<S, C extends FacetConfiguration & PersistentStateComponent<S>> extends FacetEditorTab {
    private final FacetEditorContext editorContext;
    private final FacetValidatorsManager validatorsManager;

    protected FacetPropertiesEditorTab(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        this.editorContext = editorContext;
        this.validatorsManager = validatorsManager;
    }

    public Project project() {
        return editorContext.getProject();
    }

    @NotNull
    public Module module() {
        return editorContext.getModule();
    }

    public FacetValidatorsManager validatorsManager() {
        return validatorsManager;
    }

    public ModifiableRootModel modifiableRootModel() {
        return editorContext.getModifiableRootModel();
    }

    protected Facet<C> facet() {
        //noinspection unchecked
        return (Facet<C>) editorContext.getFacet();
    }

    public S facetState() {
        return facet().getConfiguration().getState();
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(facetState(), currentState());
    }

    public abstract S currentState();
}

