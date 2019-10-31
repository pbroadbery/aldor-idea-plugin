package aldor.builder;

import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

public final class AldorTargetIds {

    public static String aldorFileTargetId(String path) {
        return path;
    }

    public static String aldorModuleTargetId(String moduleName) {
        return moduleName;
    }

    public static String aldorJarTargetId(JpsModuleSourceRoot sourceRoot) {
        return sourceRoot.getFile().toString();
    }
}
