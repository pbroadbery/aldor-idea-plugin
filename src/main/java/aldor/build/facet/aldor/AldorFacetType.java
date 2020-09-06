package aldor.build.facet.aldor;

import aldor.build.module.AldorModuleType;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AldorFacetType extends FacetType<AldorFacet, AldorFacetConfiguration> {
    public static final FacetTypeId<AldorFacet> TYPE_ID = new FacetTypeId<>(AldorFacetConstants.ID);

    public AldorFacetType() {
        super(TYPE_ID, AldorFacetConstants.ID, AldorFacetConstants.NAME);
    }

    public static FacetType<AldorFacet, AldorFacetConfiguration> instance() {
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
}

