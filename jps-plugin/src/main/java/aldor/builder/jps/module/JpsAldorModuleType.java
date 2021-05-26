package aldor.builder.jps.module;

import aldor.builder.jps.AldorModuleExtensionRole;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsElementTypeWithDefaultProperties;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.module.JpsModuleType;

public class JpsAldorModuleType implements JpsModuleType<JpsSimpleElement<AldorModuleState>>, JpsElementTypeWithDefaultProperties<JpsSimpleElement<AldorModuleState>> {
    private static final Logger LOG = Logger.getInstance(JpsAldorModuleType.class);
    public static final JpsAldorModuleType INSTANCE = new JpsAldorModuleType();
    public static final String ID = "ALDOR-MODULE";

    @NotNull
    @Override
    public JpsElementChildRole<JpsSimpleElement<AldorModuleState>> getPropertiesRole() {
        return AldorModuleExtensionRole.INSTANCE;
    }

    @NotNull
    @Override
    public JpsSimpleElement<AldorModuleState> createDefaultProperties() {
        return JpsElementFactory.getInstance().createSimpleElement(AldorModuleState.newBuilder()
                .build());
    }
}