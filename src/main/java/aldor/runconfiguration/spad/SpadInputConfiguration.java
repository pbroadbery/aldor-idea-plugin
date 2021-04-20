package aldor.runconfiguration.spad;

import aldor.build.facet.fricas.FricasFacet;
import aldor.build.facet.fricas.FricasFacetType;
import aldor.sdk.fricas.FricasSdkType;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * Configuration - the settings (file name, how to run, etc)
 */
public class SpadInputConfiguration
        extends ModuleBasedConfiguration<RunConfigurationModule, Element>
        implements SpadRunProfile, RunConfigurationWithSuppressedDefaultDebugAction {
    private final SpadInputRunConfigurationType.SpadInputConfigurationBean bean = new SpadInputRunConfigurationType.SpadInputConfigurationBean();

    public SpadInputConfiguration(String name, @NotNull RunConfigurationModule configurationModule, @NotNull ConfigurationFactory factory) {
        super(name, configurationModule, factory);
    }

    public SpadInputConfiguration(RunConfigurationModule configurationModule, ConfigurationFactory factory) {
        this("", configurationModule, factory);
    }

    public SpadInputRunConfigurationType.SpadInputConfigurationBean bean() {
        return bean;
    }

    @Override
    public Collection<Module> getValidModules() {
        // TODO: Filter by type
        return Arrays.asList(ModuleManager.getInstance(getProject()).getModules());
    }

    @NotNull
    @Override
    public SettingsEditor<SpadInputConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<SpadInputConfiguration> group = new SettingsEditorGroup<>();

        group.addEditor("Run details", new SpadInputConfigurable(getProject(), getConfigurationModule().getModule(), effectiveSdk()));

        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new SpadInputProfileState(this, environment);
    }

    public void inputFile(String inputFile) {
        this.bean.inputFile = inputFile;
    }

    public String inputFile() {
        return bean.inputFile;
    }

    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    @Nullable
    public Sdk configuredSdk() {
        // Ideally, we'd check with the form...
        if (getConfigurationModule().getModule() == null) {
            return null;
        }
        @Nullable FricasFacet facet = FacetManager.getInstance(getConfigurationModule().getModule()).getFacetByType(FricasFacetType.TYPE_ID);
        if (facet == null) {
            return null;
        }
        return facet.getConfiguration().sdk();
    }

    @Nullable
    public Sdk effectiveSdk() {
        Sdk mySdk = configuredSdk();
        if (mySdk == null) {
            return null;
        }
        if (mySdk.getSdkType() instanceof FricasSdkType) {
            return mySdk;
        } else {
            for (Sdk sdk : ProjectJdkTable.getInstance().getAllJdks()) {
                if (sdk.getSdkType() instanceof FricasSdkType) {
                    return sdk;
                }
            }
        }
        return null;
    }

    private static final String SPAD_INPUT_CONF_ELT = "SpadInput";
    private static final String SPAD_INPUT_FILE = "inputFile";
    private static final String SPAD_LOAD_LOCAL = "loadLocal";
    private static final String SPAD_KEEP_RUNNING = "keepRunning";

    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public void writeExternal(@NotNull Element parentElement) throws WriteExternalException {
        super.writeExternal(parentElement);
        final Element element = new Element(SPAD_INPUT_CONF_ELT);
        parentElement.addContent(element);
        element.setAttribute(SPAD_INPUT_FILE, (bean.inputFile == null) ? "" : bean.inputFile);
        element.setAttribute(SPAD_LOAD_LOCAL, Boolean.toString(bean.loadSpad));
        element.setAttribute(SPAD_KEEP_RUNNING, Boolean.toString(bean.keepRunning));
    }

    @SuppressWarnings("ThrowsRuntimeException")
    @Override
    public void readExternal(@NotNull Element parentElement) throws InvalidDataException {
        super.readExternal(parentElement);
        final Element element = parentElement.getChild(SPAD_INPUT_CONF_ELT);
        if (element == null) {
            throw new InvalidDataException("Missing configuration element");
        }
        this.bean.inputFile = element.getAttributeValue(SPAD_INPUT_FILE);
        this.bean.loadSpad = Boolean.parseBoolean(element.getAttributeValue(SPAD_LOAD_LOCAL));
        this.bean.keepRunning = Boolean.parseBoolean(element.getAttributeValue(SPAD_KEEP_RUNNING));
    }
}
