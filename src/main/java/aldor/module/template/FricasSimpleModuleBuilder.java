package aldor.module.template;

import aldor.build.facet.fricas.FricasFacet;
import aldor.build.facet.fricas.FricasFacetProperties;
import aldor.build.module.AldorModuleBuilder;
import aldor.build.module.AldorModuleType;
import aldor.sdk.fricas.FricasInstalledSdkType;
import com.google.common.collect.Maps;
import com.intellij.ide.util.projectWizard.WizardInputField;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static aldor.module.template.TemplateFiles.saveFile;

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
        fields.add(new WizardFieldDocumentation(SELECTED_SDK_NAME_ID + "DOC",
                "This is the location of your FriCAS home directory." +
                        "  Typically something like /usr/lib/fricas/target/x86_64-linux-gnu. " +
                        "Selecting the '+ Add FriCAS SDK' option in the drop down will let you add a new location."));
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
        return "FriCAS Module";
    }

    @Override
    public String getDescription() {
        return "<p>FriCAS module with SPAD and input files<br>\n" +
                "<list>Features:\n" +
                " <li> Syntax Highlighting\n" +
                " <li> Type Browsing (a la Hyperdoc)\n" +
                " <li> Running .input files\n" +
                " <li> Interactive Fricas sessions" +
                "</list>";
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
    public void setupRootModel(@NotNull final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        super.setupRootModel(modifiableRootModel);
        // From this point, call
        //    ContentEntry entries = modifiableRootModel.getContentEntries()[0];
        // to get current content entry
        createFileLayout(Objects.requireNonNull(modifiableRootModel.getContentEntries()[0].getFile()), modifiableRootModel);
    }

    private void createFileLayout(VirtualFile contentRootDir, ModifiableRootModel model) throws ConfigurationException {
        Map<String, String> map = Maps.newHashMap();
        map.put("PROJECT", model.getProject().getName());
        map.put("MODULE", model.getModule().getName());
        map.put("ALDOR_SDK", (model.getSdk() == null) ? "\"SDK path goes here\"" : model.getSdk().getHomePath());
        map.put("INITIAL_ALDOR_FILES", "example");

        @Nullable VirtualFile file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath(), "example.spad");
        if (file == null) {
            return;
        }
        saveFile(model.getProject(), file, "SPAD Initial.spad", map);

        file = getOrCreateExternalProjectConfigFile(contentRootDir.getPath(), "example.input");
        if (file == null) {
            return;
        }
        saveFile(model.getProject(), file, "Fricas Initial.input", map);
    }

    @Nullable
    private static VirtualFile getOrCreateExternalProjectConfigFile(@NotNull String parent, @NotNull String fileName) {
        File file = new File(parent, fileName);
        FileUtilRt.createIfNotExists(file);
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }

    FricasFacetProperties properties() {
        return new FricasFacetProperties(fields.value(String.class, SELECTED_SDK_NAME_ID));
    }

}
