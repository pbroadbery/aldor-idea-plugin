package aldor.module.template;

import aldor.build.facet.fricas.FricasFacet;
import aldor.build.facet.fricas.FricasFacetProperties;
import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleType;
import aldor.sdk.fricas.FricasInstalledSdkType;
import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

class FricasSimpleModuleBuilder extends AldorModuleBuilder {
    private static final Logger LOG = Logger.getInstance(FricasSimpleModuleBuilder.class);
    private static final String SELECTED_SDK_NAME_ID = "SelectedSdk";
    private final WizardFieldContainer fields = new WizardFieldContainer();

    protected FricasSimpleModuleBuilder() {
        super(AldorModuleType.instance());
        createAdditionalFields();
    }

    private void createAdditionalFields() {
        fields.add(new WizardJdkSelector(SELECTED_SDK_NAME_ID, "Fricas Version", null,
                                            Collections.singleton(FricasInstalledSdkType.instance())));
    }

    @Override
    public String getName() {
        return "SimpleFricas";
    }

    @Override
    protected @NotNull List<WizardInputField<?>> getAdditionalFields() {
        return fields.fields();
    }

    @Override
    public String getPresentableName() {
        return "Simple Fricas module";
    }

    @Override
    public String getDescription() {
        return "Fricas module containing .spad sources.  This supports type browsing, creating interactive sessions and syntax highlighting";
    }

    @Override
    protected void setupModule(Module module) throws ConfigurationException {
        LOG.debug("setup module.. facet " + properties().sdkName());
        FricasFacet.createFacetIfMissing(module, properties());
        LOG.debug("setup module.. module");
        super.setupModule(module);
        LOG.debug("setup module.. done");
    }

    @Override
    public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        super.setupRootModel(modifiableRootModel);
        // From this point, call
        //    ContentEntry entries = modifiableRootModel.getContentEntries()[0];
        // to get current content entry
        createFileLayout(modifiableRootModel.getContentEntries()[0]);
    }

    private void createFileLayout(ContentEntry entry) {
    }

    FricasFacetProperties properties() {
        return new FricasFacetProperties(fields.value(String.class, SELECTED_SDK_NAME_ID));
    }

}
