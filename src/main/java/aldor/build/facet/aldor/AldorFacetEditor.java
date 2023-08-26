package aldor.build.facet.aldor;

import aldor.build.facet.FacetPropertiesEditorTab;
import aldor.builder.jps.module.AldorFacetProperties;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetValidatorsManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class AldorFacetEditor extends FacetPropertiesEditorTab<AldorFacetProperties, AldorFacetConfiguration> {
    private AldorFacetEditorForm form = null;

    public AldorFacetEditor(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        super(editorContext, validatorsManager);
    }

    @NotNull
    @Override
    public JComponent createComponent() {
        if (form == null) {
            form = new AldorFacetEditorForm(this);
        }
        return form.topPanel();
    }

    @Override
    public void apply() {
        facet().getConfiguration().updateState(currentState());
    }

    @Override
    public void reset() {
        form.reset();
    }

    @Override
    public String getDisplayName() {
        return "Aldor Facet Editor";
    }


    @Override
    @NotNull
    public AldorFacetProperties currentState() {
        return form.currentState();
    }
    @VisibleForTesting
    public AldorFacetEditorForm form() {
        return form;
    }
}