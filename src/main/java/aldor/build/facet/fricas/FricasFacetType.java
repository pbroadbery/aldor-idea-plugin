package aldor.build.facet.fricas;

import aldor.build.module.AldorModuleType;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FricasFacetType extends FacetType<FricasFacet, FricasFacetConfiguration> {
    public static final String FRICAS_FACET_ID = "Fricas Facet";
    public static final String FRICAS_FACET_NAME = "Fricas";
    public static final FacetTypeId<FricasFacet> TYPE_ID = new FacetTypeId<>(FRICAS_FACET_ID);

    public FricasFacetType() {
        super(TYPE_ID, FRICAS_FACET_ID, FRICAS_FACET_NAME);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static FacetType<FricasFacet, FricasFacetConfiguration> instance() {
        return findInstance(FricasFacetType.class);
    }

    @Override
    public FricasFacetConfiguration createDefaultConfiguration() {
        return new FricasFacetConfiguration();
    }

    @Override
    public FricasFacet createFacet(@NotNull com.intellij.openapi.module.Module module, String name, @NotNull FricasFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new FricasFacet(this, module, name, configuration, underlyingFacet);
    }

    @Override
    public boolean isSuitableModuleType(ModuleType moduleType) {
        return moduleType instanceof AldorModuleType;
    }
}

