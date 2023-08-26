package aldor.builder.jps.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.module.JpsModule;

public class JpsConfiguredRootFacetExtension extends JpsCompositeElementBase<JpsConfiguredRootFacetExtension> {
    public static final JpsElementChildRole<JpsConfiguredRootFacetExtension> ROLE = JpsElementChildRoleBase.create("ConfiguredRootFacetProperties");

    private final ConfigRootFacetProperties myProperties;

    @SuppressWarnings("UnusedDeclaration")
    public JpsConfiguredRootFacetExtension() {
        myProperties = new ConfigRootFacetProperties(false, "--NotSet--", "--NotSet--");
    }

    public JpsConfiguredRootFacetExtension(ConfigRootFacetProperties properties) {
        myProperties = properties;
    }

    private JpsConfiguredRootFacetExtension(JpsConfiguredRootFacetExtension moduleExtension) {
        myProperties = moduleExtension.getProperties().asBuilder().build();
    }

    @NotNull
    @Override
    public JpsConfiguredRootFacetExtension createCopy() {
        return new JpsConfiguredRootFacetExtension(this);
    }

    public ConfigRootFacetProperties getProperties() {
        return myProperties;
    }

    @Nullable
    public static JpsConfiguredRootFacetExtension getExtension(@SuppressWarnings("TypeMayBeWeakened") @Nullable JpsModule module) {
        return (module != null) ? module.getContainer().getChild(ROLE) : null;
    }

    @VisibleForTesting
    public void install(JpsModule module) {
        module.getContainer().setChild(ROLE, this);
    }

    @Override
    public String toString() {
        return "JpsConfiguredRootFacetExtension{" +
                "myProperties=" + myProperties +
                '}';
    }
}
