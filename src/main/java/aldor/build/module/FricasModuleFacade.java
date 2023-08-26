package aldor.build.module;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.fricas.FricasFacet;
import aldor.build.facet.fricas.FricasFacetProperties;
import aldor.build.facet.fricas.FricasFacetType;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;

import java.util.Optional;

public class FricasModuleFacade implements SpadModuleFacade {
    private final Module module;
    private final FricasFacet facet;

    FricasModuleFacade(Module module) {
        this.module = module;
        this.facet = FacetManager.getInstance(module).getFacetByType(FricasFacetType.instance().getId());
    }

    FricasFacet facet() {
        return facet;
    }

    public Optional<FricasFacetProperties> properties() {
        return facet().getProperties();
    }

    public static boolean isAldorModule(Module module) {
        AldorFacet facet = AldorFacet.forModule(module);
        return facet != null;
    }

    public static Optional<AldorModuleFacade> forModule(Module module) {
        return isAldorModule(module) ? Optional.of(new AldorModuleFacade(module)): Optional.empty();
    }

    @Override
    public boolean isConfigured() {
        return false;
    }

}
