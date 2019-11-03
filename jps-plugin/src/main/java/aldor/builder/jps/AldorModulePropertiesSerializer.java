package aldor.builder.jps;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.serialization.module.JpsModulePropertiesSerializer;

public class AldorModulePropertiesSerializer extends JpsModulePropertiesSerializer<JpsSimpleElement<JpsAldorModuleProperties>> {

    protected AldorModulePropertiesSerializer() {
        super(JpsAldorModuleType.INSTANCE, "ALDOR-MODULE", "AldorModuleBuildPath");
    }

    @Override
    public JpsSimpleElement<JpsAldorModuleProperties> loadProperties(@Nullable Element componentElement) {
        if (componentElement == null) {
            return JpsElementFactory.getInstance().createSimpleElement(new JpsAldorModuleProperties("", JpsAldorMakeDirectoryOption.Invalid));
        }
        String directory = componentElement.getAttributeValue("outputDirectory");
        String makeOptionName = componentElement.getAttributeValue("makeDirectory");

        JpsAldorMakeDirectoryOption makeOption;
        try {
            makeOption = JpsAldorMakeDirectoryOption.valueOf(makeOptionName);
        }
        catch (IllegalArgumentException e) {
            makeOption = JpsAldorMakeDirectoryOption.Invalid;
        }

        return JpsElementFactory.getInstance().createSimpleElement(new JpsAldorModuleProperties(directory, makeOption));
    }

    @Override
    public void saveProperties(@NotNull JpsSimpleElement<JpsAldorModuleProperties> properties, @NotNull Element componentElement) {

    }
}
