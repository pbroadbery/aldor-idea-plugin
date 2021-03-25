package aldor.runconfiguration.aldor;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import icons.AldorIcons;
import org.jetbrains.annotations.NotNull;

public class AldorUnitRunConfigurationType extends ConfigurationTypeBase {

    @NotNull
    public static AldorUnitRunConfigurationType instance() {
        return CONFIGURATION_TYPE_EP.findExtensionOrFail(AldorUnitRunConfigurationType.class);
    }

    public AldorUnitRunConfigurationType() {
        super("AldorUnitRunConfigurationType", "Aldor Unit Test", "Execute an Aldor Unit Test", AldorIcons.ALDOR_FILE);
        addFactory(new AldorUnitRunConfigurationFactory());
    }

    public ConfigurationFactory factory() {
        return this.getConfigurationFactories()[0];
    }

    private class AldorUnitRunConfigurationFactory extends ConfigurationFactory {

        protected AldorUnitRunConfigurationFactory() {
            super(AldorUnitRunConfigurationType.this);
        }

        @NotNull
        @Override
        public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
            AldorRunConfigurationModule templateModule = new AldorRunConfigurationModule(project);
            return new AldorUnitConfiguration(templateModule, this);
        }

        @NotNull
        @Override
        public String getId() {
            return instance().getId();
        }
    }

}
