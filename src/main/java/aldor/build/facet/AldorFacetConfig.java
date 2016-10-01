package aldor.build.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

/**
 * Created by pab on 26/09/16.
 */
public class AldorFacetConfig implements FacetConfiguration, PersistentStateComponent<AldorFacetConfig.AldorFacetState> {
    private String buildDirectory = null;

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[] {new AldorFacetEditorTab(editorContext)};
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        // not implemented: Deprecated.
    }

    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        // not implemented: Deprecated.
    }

    @Nullable
    @Override
    public AldorFacetState getState() {
        return new AldorFacetState(buildDirectory);
    }

    @Override
    public void loadState(AldorFacetState state) {
        this.buildDirectory = state.buildDirectory;
    }

    public void buildDirectory(String text) {
        this.buildDirectory = text;
    }

    public String buildDirectory() {
        return buildDirectory;
    }

    @SuppressWarnings("PublicField")
    public static class AldorFacetState {
        public final String buildDirectory;

        AldorFacetState() {
            this(".");
        }

        AldorFacetState(String buildDirectory) {
            this.buildDirectory = buildDirectory;
        }
    }

}
