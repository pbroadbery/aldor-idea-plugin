package aldor.build.facet.aldor;

import aldor.builder.jps.module.AldorFacetExtensionProperties;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jdom.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class AldorFacetConfigurationTest {

    @Test
    public void test() {
        AldorFacetConfiguration config = new AldorFacetConfiguration();
        AldorFacetExtensionProperties state = config.getState().asBuilder().setRelativeOutputDirectory("hello").build();
        Element xml = XmlSerializer.serialize(state);
        System.out.println("xml: "+ xml);
        AldorFacetExtensionProperties p2 = XmlSerializer.deserialize(xml, AldorFacetExtensionProperties.class);
        assertEquals(state, p2);
    }
}