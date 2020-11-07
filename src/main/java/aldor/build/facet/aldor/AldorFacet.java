package aldor.build.facet.aldor;

import aldor.build.facet.ModuleModifyingFacetUtil;
import aldor.build.facet.SpadFacet;
import aldor.builder.jps.AldorModuleExtensionProperties;
import aldor.sdk.aldor.AldorInstalledSdkType;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AldorFacet extends ModuleModifyingFacet<AldorFacetConfiguration> implements SpadFacet<AldorModuleExtensionProperties> {
    private static final Logger LOG = Logger.getInstance(AldorFacet.class);

    public AldorFacet(@NotNull FacetType facetType, @NotNull Module module, @NotNull String name, @NotNull AldorFacetConfiguration configuration,
                      @Nullable Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    public static AldorFacet forModule(Module module) {
        return FacetManager.getInstance(module).getFacetByType(AldorFacetType.instance().getId());
    }

    @Override
    public void updateModule() {
        ModuleModifyingFacetUtil.updateLibrary(getModule(), getConfiguration().sdk(), AldorInstalledSdkType.instance());
    }

    @Override
    public void facetRemoved() {
        ModuleModifyingFacetUtil.removeLibrary(AldorInstalledSdkType.instance(), getModule());
    }

    //should only be called from write action
    public static AldorFacet createFacetIfMissing(@NotNull Module module, AldorModuleExtensionProperties properties) {
        FacetManager facetManager = FacetManager.getInstance(module);
        AldorFacetType ft = FacetType.findInstance(AldorFacetType.class);
        AldorFacet prev = facetManager.getFacetByType(ft.getId());
        if (prev != null) {
            return prev;
        }
        AldorFacet facet = facetManager.createFacet(ft, AldorFacetConstants.NAME, null);
        ModifiableFacetModel facetModel = facetManager.createModifiableModel();
        facetModel.addFacet(facet);
        facetModel.commit();
        facet.getConfiguration().loadState(properties);
        FacetManager.getInstance(module).facetConfigurationChanged(facet);
        return facet;
    }

    @Override
    public void initFacet() {
        LOG.info("Initialising facet " + this.getTypeId());
        super.initFacet();
        updateModule();
    }

    @Override
    public void disposeFacet() {
        LOG.info("Disposing facet " + this.getTypeId());
        super.disposeFacet();
    }


    @Override
    public Optional<AldorModuleExtensionProperties> getProperties() {
        return Optional.ofNullable(getConfiguration().getState());
    }
}
