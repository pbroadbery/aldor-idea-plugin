package aldor.builder.jps;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

public final class AldorSourceRootType implements JpsModuleSourceRootType<AldorSourceRootProperties> {
    private static final Logger LOG = Logger.getInstance(AldorSourceRootType.class);
    public static final AldorSourceRootType INSTANCE = new AldorSourceRootType(false);
    public static final AldorSourceRootType TEST = new AldorSourceRootType(true);
    private final boolean isTest;

    private AldorSourceRootType(boolean isTest) {
        this.isTest = isTest;
    }

    public static boolean isMainInstance(JpsModuleSourceRootType<?> rootType) {
        if (rootType instanceof AldorSourceRootType) {
            return !((AldorSourceRootType) rootType).isTest;
        }
        return false;
    }

    public static boolean isTestInstance(JpsModuleSourceRootType<?> rootType) {
        if (rootType instanceof AldorSourceRootType) {
            return ((AldorSourceRootType) rootType).isTest;
        }
        return false;
    }

    @Override
    public boolean isForTests() {
        return isTest;
    }

    @Override
    public @NotNull JpsElementChildRole<AldorSourceRootProperties> getPropertiesRole() {
        return AldorSourceRootRole.INSTANCE;
    }

    @Override
    public @NotNull AldorSourceRootProperties createDefaultProperties() {
        return new AldorSourceRootProperties("file://$MODULE_DIR$/src/main/aldor");
    }

}