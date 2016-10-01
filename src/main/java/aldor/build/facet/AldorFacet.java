package aldor.build.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by pab on 26/09/16.
 */
public class AldorFacet extends Facet<AldorFacetConfig> {
    public static final FacetTypeId<AldorFacet> ID = new FacetTypeId<>(AldorFacetType.ID);

    @Nullable
    public static AldorFacet getInstance(final Module module) {
        return FacetManager.getInstance(module).getFacetByType(ID);
    }


    public AldorFacet(@NotNull FacetType<AldorFacet, AldorFacetConfig> facetType, @NotNull Module module, @NotNull String name,
                      @NotNull AldorFacetConfig configuration, Facet<?> underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

}
