package aldor.build.facet.cfgroot;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;

public class ConfigRootFacet extends Facet<ConfigRootFacetConfiguration> {
    public ConfigRootFacet(@NotNull FacetType facetType, @NotNull Module module, @NotNull @NlsSafe String name, @NotNull ConfigRootFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    public static ConfigRootFacet forModule(Module module) {
        return FacetManager.getInstance(module).getFacetByType(ConfigRootFacetType.instance().getId());
    }

}
