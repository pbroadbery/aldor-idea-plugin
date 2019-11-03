package aldor.builder.jps;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JpsAldorModulePropertiesTest {

    @Test
    public void testIsValid_whenInvalid() {
        JpsAldorModuleProperties properties = new JpsAldorModuleProperties("/", JpsAldorMakeDirectoryOption.Invalid);
        assertFalse(properties.isValid());
    }

    @Test
    public void testIsValid_whenValid() {
        JpsAldorModuleProperties properties = new JpsAldorModuleProperties("/", JpsAldorMakeDirectoryOption.Source);
        assertTrue(properties.isValid());
    }

    @Test
    public void testIsValid_whenMissingDir() {
        JpsAldorModuleProperties properties = new JpsAldorModuleProperties("", JpsAldorMakeDirectoryOption.Source);
        assertFalse(properties.isValid());
    }

    @Test
    public void testIsValid_whenNullDir() {
        JpsAldorModuleProperties properties = new JpsAldorModuleProperties(null, JpsAldorMakeDirectoryOption.Source);
        assertFalse(properties.isValid());
    }
}