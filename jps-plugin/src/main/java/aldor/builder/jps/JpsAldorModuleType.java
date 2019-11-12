package aldor.builder.jps;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsElementTypeWithDefaultProperties;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleType;
import org.jetbrains.jps.model.module.JpsTypedModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static aldor.util.StringUtilsAldorRt.trimExtension;

public class JpsAldorModuleType implements JpsModuleType<JpsSimpleElement<JpsAldorModuleProperties>>, JpsElementTypeWithDefaultProperties<JpsSimpleElement<JpsAldorModuleProperties>> {
    public static final JpsAldorModuleType INSTANCE = new JpsAldorModuleType();

    @NotNull
    @Override
    public JpsElementChildRole<JpsSimpleElement<JpsAldorModuleProperties>> getPropertiesRole() {
        return AldorModuleExtensionRole.INSTANCE;
    }

    @NotNull
    @Override
    public JpsSimpleElement<JpsAldorModuleProperties> createDefaultProperties() {
        return JpsElementFactory.getInstance().createSimpleElement(new JpsAldorModuleProperties("", JpsAldorMakeDirectoryOption.Invalid));
    }

    public JpsAldorModuleProperties moduleProperties(JpsModule module) {
        JpsTypedModule<JpsSimpleElement<JpsAldorModuleProperties>> aldorModule = Objects.requireNonNull(module.asTyped(this));
        return aldorModule.getProperties().getData();

    }


    public File buildDirectory(JpsAldorModuleProperties properties, File contentRoot, File sourceRoot, File sourceFile) {
        switch (properties.makeDirectoryOption()) {
            case Source:
                return sourceRoot;
            case BuildRelative:
                File file = JpsPathUtil.urlToFile(properties.outputDirectory());
                if (file.isAbsolute()) {
                    String relativePath = FileUtilRt.getRelativePath(sourceRoot, sourceFile.getParentFile());
                    assert relativePath != null;
                    return ".".equals(relativePath) ? file : new File(file, relativePath);
                }
                else {
                    return new File(contentRoot, file.getPath());
                }
            case Invalid:
            default:
                throw new RuntimeException("Unknown");
        }
    }

    @NotNull
    @Contract(pure = true)
    public String targetName(JpsAldorModuleProperties properties, @NotNull File sourceRoot, @NotNull File file) {
        switch (properties.makeDirectoryOption()) {
            case Source:
                String url = properties.outputDirectory();
                if (url == null) {
                    return sourceTargetName(new File(sourceRoot, "out/ao"), sourceRoot, file);
                }
                else {
                    return sourceTargetName(JpsPathUtil.urlToFile(url), sourceRoot, file);
                }
            case BuildRelative:
                return buildRelativeTargetName(file);
            case Invalid:
            default:
                throw new RuntimeException("Unknown build path");
        }
    }

    @NotNull
    private static String buildRelativeTargetName(@NotNull File file) {
        return trimExtension(file.getName()) + ".ao";
    }

    @NotNull
    private static String sourceTargetName(File outputDirectory, File sourceRoot, @NotNull File file) {
        try {
            String suffix = trimExtension(file.getName()) + ".ao";
            String sourceRelativePath = FileUtilRt.getRelativePath(sourceRoot, file.getParentFile());
            String sourceRelativeSuffix = ".".equals(sourceRelativePath) ? suffix : (sourceRelativePath + "/" + suffix);
            File absoluteOutputDirectory = outputDirectory.isAbsolute() ? outputDirectory : new File(sourceRoot, outputDirectory.getPath()).getCanonicalFile();

            if (FileUtil.isAncestor(sourceRoot, absoluteOutputDirectory, false)) {
                String relativePath = FileUtilRt.getRelativePath(sourceRoot, absoluteOutputDirectory);
                return ".".equals(relativePath) ? sourceRelativeSuffix : (relativePath + "/" + sourceRelativeSuffix);
            }
            else {
                return new File(absoluteOutputDirectory, sourceRelativeSuffix).getCanonicalPath();
            }
        } catch (IOException e) {
             throw new RuntimeException("oops - " + outputDirectory + " " + sourceRoot + " " + file);
        }
    }
}