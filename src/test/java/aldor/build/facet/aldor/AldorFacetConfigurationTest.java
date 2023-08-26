package aldor.build.facet.aldor;

import aldor.builder.jps.module.AldorFacetProperties;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AldorFacetConfigurationTest {

    @Test
    public void test() {
        AldorFacetConfiguration config = new AldorFacetConfiguration();
        assertNotNull(config.getState());
        AldorFacetProperties state = config.getState().asBuilder().relativeOutputDirectory("hello").build();
        Element xml = XmlSerializer.serialize(state);
        System.out.println("xml: "+ xml);
        AldorFacetProperties p2 = XmlSerializer.deserialize(xml, AldorFacetProperties.class);
        assertEquals(state, p2);
    }
}