package aldor.builder.jps;

import aldor.build.facet.aldor.AldorFacetConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsDummyElement;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsElementTypeWithDefaultProperties;
import org.jetbrains.jps.model.library.JpsOrderRootType;
import org.jetbrains.jps.model.library.sdk.JpsSdkType;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.facet.JpsFacetConfigurationSerializer;
import org.jetbrains.jps.model.serialization.library.JpsLibraryRootTypeSerializer;
import org.jetbrains.jps.model.serialization.library.JpsSdkPropertiesSerializer;
import org.jetbrains.jps.model.serialization.module.JpsModulePropertiesSerializer;
import org.jetbrains.jps.model.serialization.module.JpsModuleSourceRootPropertiesSerializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JpsAldorModelSerializerExtension extends JpsModelSerializerExtension {
    private static final Logger LOG = Logger.getInstance(JpsAldorModelSerializerExtension.class);

    public JpsAldorModelSerializerExtension() {
        LOG.info("Creating JPS Extension");
    }

    @Override
    public void loadRootModel(@NotNull JpsModule module, @NotNull Element rootModel) {
        super.loadRootModel(module, rootModel);
    }

    @NotNull
    @Override
    public List<? extends JpsModulePropertiesSerializer<?>> getModulePropertiesSerializers() {
        return Collections.singletonList(new AldorModulePropertiesSerializer());
    }

    @Override
    public void loadModuleOptions(@NotNull JpsModule module, @NotNull Element rootElement) {
        super.loadModuleOptions(module, rootElement);
    }

    @NotNull
    @Override
    public List<? extends JpsSdkPropertiesSerializer<?>> getSdkPropertiesSerializers() {
        return Arrays.asList(AldorLocalSdkPropertiesSerializer.installed, AldorLocalSdkPropertiesSerializer.local);
    }

    @NotNull
    @Override
    public List<JpsLibraryRootTypeSerializer> getSdkRootTypeSerializers() {
        return Arrays.asList(new AldorLocalSdkRootTypeSerializer(), new AldorInstalledSdkRootTypeSerializer());
    }

    private static final class AldorLocalSdkRootTypeSerializer extends JpsLibraryRootTypeSerializer {
        private AldorLocalSdkRootTypeSerializer() {
            super("Aldor Local SDK", JpsOrderRootType.COMPILED, true);
        }
    }

    private static final class AldorInstalledSdkRootTypeSerializer extends JpsLibraryRootTypeSerializer {
        private AldorInstalledSdkRootTypeSerializer() {
            super("Aldor SDK", JpsOrderRootType.COMPILED, true);
        }
    }

    @NotNull
    @Override
    public List<? extends JpsFacetConfigurationSerializer<?>> getFacetConfigurationSerializers() {
        return Collections.singletonList(new JpsAldorFacetConfigurationSerializer());
    }

    @Override
    public @NotNull List<? extends JpsModuleSourceRootPropertiesSerializer<?>> getModuleSourceRootPropertiesSerializers() {
        return Collections.singletonList(new JpsAldorSourceRootSerializer());
    }

    private static class AldorLocalSdkPropertiesSerializer extends JpsSdkPropertiesSerializer<JpsDummyElement>{
        public static final AldorLocalSdkPropertiesSerializer local = new AldorLocalSdkPropertiesSerializer("Aldor Local SDK", JpsAldorSdkType.LOCAL);
        public static final AldorLocalSdkPropertiesSerializer installed = new AldorLocalSdkPropertiesSerializer("Aldor SDK", JpsAldorSdkType.INSTALLED);

        protected AldorLocalSdkPropertiesSerializer(String name, JpsAldorSdkType type) {
            super(name, type);
        }

        @NotNull
        @Override
        public JpsDummyElement loadProperties(@Nullable Element propertiesElement) {
            LOG.info("Loading sdk properties for: "
                    + getTypeId() + " elt --> " + Optional.ofNullable(propertiesElement).map(Element::getAttributes).orElse(null));
            return JpsElementFactory.getInstance().createDummyElement();
        }

        @Override
        public void saveProperties(@NotNull JpsDummyElement properties, @NotNull Element element) {
            LOG.info("Saving sdk properties for: " + getTypeId() + " " + element + " " + properties);
        }

    }

    private enum AldorSdkSubType { local, installed }
    public static class JpsAldorSdkType extends JpsSdkType<JpsDummyElement> implements JpsElementTypeWithDefaultProperties<JpsDummyElement> {
        public static final JpsAldorSdkType LOCAL = new JpsAldorSdkType(AldorSdkSubType.local);
        public static final JpsAldorSdkType INSTALLED = new JpsAldorSdkType(AldorSdkSubType.installed);
        private final AldorSdkSubType subType;

        JpsAldorSdkType(AldorSdkSubType subType) {
            this.subType = subType;
        }

        @NotNull
        @Override
        public JpsDummyElement createDefaultProperties() {
            return JpsElementFactory.getInstance().createDummyElement();
        }
    }

    private static final class JpsAldorFacetConfigurationSerializer extends JpsFacetConfigurationSerializer<JpsAldorModuleExtension> {
        private JpsAldorFacetConfigurationSerializer() {
            super(JpsAldorModuleExtension.ROLE, AldorFacetConstants.ID, AldorFacetConstants.NAME);
        }

        @Override
        protected JpsAldorModuleExtension loadExtension(@NotNull Element facetConfigElement, String name, JpsElement parent, JpsModule module) {
            AldorModuleExtensionProperties props = XmlSerializer.deserialize(facetConfigElement, AldorModuleExtensionProperties.class);
            LOG.info("Loaded facet extension " + props + " " + props.buildJavaComponents());
            return new JpsAldorModuleExtension(props);
        }
    }

    private class JpsAldorSourceRootSerializer extends JpsModuleSourceRootPropertiesSerializer<AldorSourceRootProperties> {
        JpsAldorSourceRootSerializer() {
            super(AldorSourceRootType.INSTANCE, "AldorSource");
        }

        @Override
        public AldorSourceRootProperties loadProperties(@NotNull Element sourceRootTag) {
            return new AldorSourceRootProperties(sourceRootTag.getAttributeValue("outputDirectory"));
        }

        @Override
        public void saveProperties(@NotNull AldorSourceRootProperties properties, @NotNull Element sourceRootTag) {
            sourceRootTag.setAttribute("outputDirectory", properties.outputDirectory());
        }
    }
}
