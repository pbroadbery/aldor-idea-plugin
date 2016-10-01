package aldor.build.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by pab on 26/09/16.
 */
public class AldorFacetType extends FacetType<AldorFacet, AldorFacetConfig> {

    public static final String ID = "AldorFacet";
    public static final String NAME = "Aldor - Facet";

    AldorFacetType() {
        super(AldorFacet.ID, ID, NAME, null);
    }

    @Override
    public AldorFacetConfig createDefaultConfiguration() {
        return new AldorFacetConfig();
    }

    @Override
    public AldorFacet createFacet(@NotNull Module module, String name, @NotNull AldorFacetConfig configuration,
                                  @SuppressWarnings("rawtypes") @Nullable Facet underlyingFacet) {
        return new AldorFacet(this, module, name, configuration, underlyingFacet);
    }

    @Override
    public boolean isSuitableModuleType(@SuppressWarnings("rawtypes") ModuleType moduleType) {
        return true;
    }

    public static FacetType<AldorFacet, AldorFacetConfig> instance() {
        return new AldorFacetType();
    }
}
