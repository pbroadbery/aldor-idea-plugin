package aldor.build.facet.fricas;

import aldor.build.facet.ModuleModifyingFacetUtil;
import aldor.build.facet.SpadFacet;
import aldor.build.facet.aldor.ModuleModifyingFacet;
import aldor.sdk.fricas.FricasInstalledSdkType;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FricasFacet extends ModuleModifyingFacet<FricasFacetConfiguration> implements SpadFacet<FricasFacetProperties> {
    public FricasFacet(@NotNull FacetType facetType, @NotNull Module module, @NotNull String name, @NotNull FricasFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    @Override
    public void initFacet() {
        updateModule();
    }

    @Override
    public void updateModule() {
        ModuleModifyingFacetUtil.updateLibrary(getModule(), getConfiguration().sdk(), FricasInstalledSdkType.instance());
    }

    @Override
    public void facetRemoved() {
        ModuleModifyingFacetUtil.removeLibrary(FricasInstalledSdkType.instance(), getModule());

    }

    public static FricasFacet createFacetIfMissing(Module module, FricasFacetProperties properties) {
        FacetManager facetManager = FacetManager.getInstance(module);
        FacetType<FricasFacet, FricasFacetConfiguration> ft = FricasFacetType.instance();
        FricasFacet prev = facetManager.getFacetByType(ft.getId());
        if (prev != null) {
            return prev;
        }
        FricasFacet facet = facetManager.createFacet(ft, FricasFacetType.FRICAS_FACET_NAME, null);
        facet.getConfiguration().loadState(properties);
        ModifiableFacetModel facetModel = facetManager.createModifiableModel();
        facetModel.addFacet(facet);
        facetModel.commit();
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
