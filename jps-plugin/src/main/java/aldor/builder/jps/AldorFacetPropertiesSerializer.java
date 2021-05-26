package aldor.builder.jps;

import com.intellij.openapi.diagnostic.Logger;

public class AldorFacetPropertiesSerializer { //extends JpsModulePropertiesSerializer<JpsSimpleElement<AldorFacetExtensionProperties>> {
    private static final Logger LOG = Logger.getInstance(AldorFacetPropertiesSerializer.class);
/*
    protected AldorFacetPropertiesSerializer() {
        super(JpsAldorModuleType.INSTANCE, "ALDOR-MODULE", "AldorModulePropertiesSerializer");
    }

    @Override
    public JpsSimpleElement<AldorFacetExtensionProperties> loadProperties(@Nullable Element componentElement) {
        LOG.info("Loading properties "+ componentElement);
        AldorFacetExtensionProperties props = (componentElement != null) ? XmlSerializer.deserialize(componentElement, AldorFacetExtensionProperties.class) : null;
        return JpsElementFactory.getInstance().createSimpleElement((props == null) ? new AldorFacetExtensionProperties() : props);
    }

    @Override
    public void saveProperties(@NotNull JpsSimpleElement<AldorFacetExtensionProperties> properties, @NotNull Element componentElement) {
        LOG.info("Saving properties "+ properties.getData());
        XmlSerializer.serializeInto(properties.getData(), componentElement);
    }
 */
}
