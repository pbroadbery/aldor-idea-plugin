package aldor.build.facet.cfgroot;

import aldor.builder.jps.module.ConfigRootFacetProperties;
import com.intellij.configurationStore.XmlSerializer;
import org.jdom.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigRootFacetConfigurationTest {

    @Test
    public void testDefault() {
        ConfigRootFacetConfiguration config = new ConfigRootFacetConfiguration();
        assertNotNull(config.getState());
        ConfigRootFacetProperties properties = config.getState();
        assertNotNull(properties);

        Element xml = XmlSerializer.serialize(properties, null, true);
        assertNotNull(xml);
        assertTrue(xml.toString().contains("ConfigRootFacetProperties"));
        ConfigRootFacetProperties p2 = XmlSerializer.deserialize(xml, ConfigRootFacetProperties.class);
        assertEquals(properties, p2);
    }

    @Test
    public void testSet() {
        ConfigRootFacetConfiguration config = new ConfigRootFacetConfiguration();
        assertNotNull(config.getState());
        ConfigRootFacetProperties properties = config.getState();
        assertNotNull(properties);

        config.updateState(properties.asBuilder().setBuildDirectory("foo").setInstallDirectory("bar").build());
        Element xml = XmlSerializer.serialize(config.getState(), null, true);
        assertNotNull(xml);
        ConfigRootFacetProperties p2 = XmlSerializer.deserialize(xml, ConfigRootFacetProperties.class);
        assertEquals("foo", p2.buildDirectory());
        assertEquals("bar", p2.installDirectory());
    }
}