package aldor.build.facet.aldor;

import aldor.build.module.AldorModuleType;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AldorFacetType extends FacetType<AldorFacet, AldorFacetConfiguration> {
    public static final FacetTypeId<AldorFacet> TYPE_ID = new FacetTypeId<>(AldorFacetConstants.ALDOR_FACET_ID);

    public AldorFacetType() {
        super(TYPE_ID, AldorFacetConstants.ALDOR_FACET_ID, AldorFacetConstants.ALDOR_FACET_NAME);
    }

    public static AldorFacetType instance() {
        return findInstance(AldorFacetType.class);
    }

    @Override
    public AldorFacetConfiguration createDefaultConfiguration() {
        return new AldorFacetConfiguration();
    }

    @Override
    public AldorFacet createFacet(@NotNull Module module, String name, @NotNull AldorFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new AldorFacet(this, module, name, configuration, underlyingFacet);
    }

    @Override
    public boolean isSuitableModuleType(ModuleType moduleType) {
        return moduleType instanceof AldorModuleType;
    }

    public Optional<AldorFacet> facetIfPresent(Module module) {
        return Optional.ofNullable(FacetManager.getInstance(module).getFacetByType(this.getId()));
    }
}

