package aldor.builder.jps;

import com.intellij.openapi.diagnostic.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsDummyElement;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsElementTypeWithDefaultProperties;
import org.jetbrains.jps.model.library.JpsOrderRootType;
import org.jetbrains.jps.model.library.sdk.JpsSdkType;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.library.JpsLibraryRootTypeSerializer;
import org.jetbrains.jps.model.serialization.library.JpsSdkPropertiesSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JpsAldorExtension extends JpsModelSerializerExtension {
    private static final Logger LOG = Logger.getInstance(JpsAldorExtension.class);

    public JpsAldorExtension() {
        LOG.info("Creating JPS Extension");
    }


    @Override
    public void loadRootModel(@NotNull JpsModule module, @NotNull Element rootModel) {
        super.loadRootModel(module, rootModel);
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

    private class AldorLocalSdkRootTypeSerializer extends JpsLibraryRootTypeSerializer {
        public AldorLocalSdkRootTypeSerializer() {
            super("Aldor Local SDK", JpsOrderRootType.COMPILED, true);
        }
    }

    private class AldorInstalledSdkRootTypeSerializer extends JpsLibraryRootTypeSerializer {
        public AldorInstalledSdkRootTypeSerializer() {
            super("Aldor SDK", JpsOrderRootType.COMPILED, true);
        }
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
            LOG.info("Loading properties for: "
                    + getTypeId() + " elt --> " + Optional.ofNullable(propertiesElement).map(Element::getAttributes).orElse(null));
            return JpsElementFactory.getInstance().createDummyElement();
        }

        @Override
        public void saveProperties(@NotNull JpsDummyElement properties, @NotNull Element element) {
            LOG.info("Saving properties for: " + getTypeId() + " " + element + " " + properties);
        }

    }

    public static class JpsAldorSdkType extends JpsSdkType<JpsDummyElement> implements JpsElementTypeWithDefaultProperties<JpsDummyElement> {
        public static final JpsAldorSdkType LOCAL = new JpsAldorSdkType();
        public static final JpsAldorSdkType INSTALLED = new JpsAldorSdkType();

        @NotNull
        @Override
        public JpsDummyElement createDefaultProperties() {
            return JpsElementFactory.getInstance().createDummyElement();
        }
    }
}
