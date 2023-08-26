package aldor.build.facet.cfgroot;

import aldor.build.facet.aldor.AldorFacetConstants;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ConfigRootFacetType extends FacetType<ConfigRootFacet, ConfigRootFacetConfiguration> {
    public static final FacetTypeId<ConfigRootFacet> TYPE_ID = new FacetTypeId<>(AldorFacetConstants.ROOT_FACET_ID);

    public ConfigRootFacetType() {
        super(TYPE_ID, AldorFacetConstants.ROOT_FACET_ID, AldorFacetConstants.ROOT_FACET_NAME);
    }

    public static ConfigRootFacetType instance() {
        return findInstance(ConfigRootFacetType.class);
    }

    @Override
    public ConfigRootFacetConfiguration createDefaultConfiguration() {
        return new ConfigRootFacetConfiguration();
    }

    @Override
    public ConfigRootFacet createFacet(@NotNull Module module, String name, @NotNull ConfigRootFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new ConfigRootFacet(this, module, name, configuration, underlyingFacet);
    }

    @Override
    public boolean isSuitableModuleType(ModuleType moduleType) {
        return true;
    }

    public Optional<ConfigRootFacet> facetIfPresent(Module module) {
        return Optional.ofNullable(FacetManager.getInstance(module).getFacetByType(this.getId()));
    }
}

