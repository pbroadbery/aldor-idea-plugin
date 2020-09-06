package aldor.build.facet.fricas;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.intellij.util.xmlb.XmlSerializer.*;
import static org.junit.Assert.*;

public class FricasFacetConfigurationTest {

    @Test
    public void testNone() {
        FricasFacetConfiguration config = new FricasFacetConfiguration();
        FricasFacetProperties props = new FricasFacetProperties();
        config.updateState(props);
        Element serial = serialize(config.getState());
        @NotNull FricasFacetProperties read = deserialize(serial, FricasFacetProperties.class);
        assertEquals(props, read);
    }

    @Test
    public void testSdk() {
        FricasFacetConfiguration config = new FricasFacetConfiguration();
        FricasFacetProperties props = new FricasFacetProperties("sdk");
        config.updateState(props);
        Element serial = serialize(config.getState());
        @NotNull FricasFacetProperties read = deserialize(serial, FricasFacetProperties.class);
        assertEquals(props, read);
    }
}