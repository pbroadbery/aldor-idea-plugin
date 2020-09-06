package aldor.build.facet.fricas;

import aldor.build.facet.SpadFacet;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FricasFacet extends Facet<FricasFacetConfiguration> implements SpadFacet<FricasFacetProperties> {
    public FricasFacet(@NotNull FacetType facetType, @NotNull Module module, @NotNull String name, @NotNull FricasFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    public static FricasFacet createFacetIfMissing(Module module, FricasFacetProperties properties) {
        FacetManager facetManager = FacetManager.getInstance(module);
        FacetType<FricasFacet, FricasFacetConfiguration> ft = FricasFacetType.instance();
        FricasFacet prev = facetManager.getFacetByType(ft.getId());
        if (prev != null) {
            return prev;
        }
        FricasFacet facet = facetManager.createFacet(ft, FricasFacetType.FRICAS_FACET_NAME, null);
        ModifiableFacetModel facetModel = facetManager.createModifiableModel();
        facetModel.addFacet(facet);
        facetModel.commit();
        facet.getConfiguration().loadState(properties);
        FacetManager.getInstance(module).facetConfigurationChanged(facet);
        return facet;

    }

    public static FricasFacet forModule(Module module) {
        return FacetManager.getInstance(module).getFacetByType(FricasFacetType.instance().getId());
    }

    @Override
    public Optional<FricasFacetProperties> getProperties() {
        return Optional.ofNullable(getConfiguration().getState());
    }
}
