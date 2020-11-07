package aldor.builder.jps;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.serialization.module.JpsModulePropertiesSerializer;

public class AldorModulePropertiesSerializer extends JpsModulePropertiesSerializer<JpsSimpleElement<AldorModuleExtensionProperties>> {
    private static final Logger LOG = Logger.getInstance(AldorModulePropertiesSerializer.class);

    protected AldorModulePropertiesSerializer() {
        super(JpsAldorModuleType.INSTANCE, "ALDOR-MODULE", "AldorModulePropertiesSerializer");
    }

    @Override
    public JpsSimpleElement<AldorModuleExtensionProperties> loadProperties(@Nullable Element componentElement) {
        LOG.info("Loading properties "+ componentElement);
        AldorModuleExtensionProperties props = (componentElement != null) ? XmlSerializer.deserialize(componentElement, AldorModuleExtensionProperties.class) : null;
        return JpsElementFactory.getInstance().createSimpleElement((props == null) ? new AldorModuleExtensionProperties() : props);
    }

    @Override
    public void saveProperties(@NotNull JpsSimpleElement<AldorModuleExtensionProperties> properties, @NotNull Element componentElement) {
        LOG.info("Saving properties "+ properties.getData().outputDirectory());
        XmlSerializer.serializeInto(properties.getData(), componentElement);
    }
}
