package aldor.builder.jps;

import aldor.builder.jps.module.AldorModuleState;
import aldor.builder.jps.module.JpsAldorModuleType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.serialization.module.JpsModulePropertiesSerializer;

import java.util.stream.Collectors;

public class AldorModuleStateSerializer extends JpsModulePropertiesSerializer<JpsSimpleElement<AldorModuleState>> {
    private static final Logger LOG = Logger.getInstance(AldorModuleStateSerializer.class);

    protected AldorModuleStateSerializer() {
        super(JpsAldorModuleType.INSTANCE,
                JpsAldorModuleType.ID, null);
    }

    @Override
    public JpsSimpleElement<AldorModuleState> loadProperties(@Nullable Element componentElement) {
        LOG.info("Loading module properties " + componentElement);
        if (componentElement == null) {
            return JpsElementFactory.getInstance().createSimpleElement(AldorModuleState.newBuilder().build());
        }
        LOG.info("Loading module properties " + componentElement + " " + componentElement.getChildren().stream().map(x -> x.getName()).collect(Collectors.joining(",")));
        AldorModuleState state = XmlSerializer.deserialize(componentElement, AldorModuleState.class);
        return JpsElementFactory.getInstance().createSimpleElement(state);
    }
}
