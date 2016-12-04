package aldor.builder;

import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class AldorBuilderServiceTest {

    @Test
    public void testCanCreateService() {
        AldorBuilderService service = new AldorBuilderService();

        assertFalse(service.createBuilders().isEmpty());
        assertFalse(service.getTargetTypes().isEmpty());

        for (TargetBuilder<?, ?> builder: service.createBuilders()) {
            assertFalse(builder.getTargetTypes().isEmpty());
        }

        for (BuildTargetType<?> targetType: service.getTargetTypes()) {
            Assert.assertNotNull(targetType.getTypeId());
        }

    }



}
