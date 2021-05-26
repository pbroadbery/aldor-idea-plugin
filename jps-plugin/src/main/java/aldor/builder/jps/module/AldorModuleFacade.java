package aldor.builder.jps.module;

import aldor.builder.jps.AldorSourceRootProperties;
import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.JpsAldorModelSerializerExtension;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.util.io.FileUtilRt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsDummyElement;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsTypedModule;
import org.jetbrains.jps.model.module.JpsTypedModuleSourceRoot;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static aldor.util.StringUtilsAldorRt.trimExtension;

public class AldorModuleFacade {
    private final AldorModuleState state;
    private final AldorFacetExtensionProperties facet;
    private final List<JpsTypedModuleSourceRoot<AldorSourceRootProperties>> roots;
    private final String aldorSdk;
    private final String javaSdk = null;

    public AldorModuleFacade(JpsModule module) {
        JpsTypedModule<JpsSimpleElement<AldorModuleState>> typedModule = module.asTyped(JpsAldorModuleType.INSTANCE);
        JpsAldorFacetExtension facetExtension = JpsAldorFacetExtension.getExtension(module);
        if ((typedModule == null) || (facetExtension == null)) {
            throw new IllegalStateException("should check that the module is an aldor module " + module.getName());
        }

        state = typedModule.getProperties().getData();
        facet = facetExtension.getProperties();
        roots = new ArrayList<>();

        module.getSourceRoots(AldorSourceRootType.INSTANCE).forEach(roots::add);
        module.getSourceRoots(AldorSourceRootType.TEST).forEach(roots::add);

        JpsSdk<JpsDummyElement> aldorSdk = module.getSdk(JpsAldorModelSerializerExtension.JpsAldorSdkType.INSTALLED);
        this.aldorSdk = Optional.ofNullable(aldorSdk).map( x-> x.getHomePath()).orElse(null);
    }

    @VisibleForTesting
    public AldorModuleFacade(AldorModuleState state, AldorFacetExtensionProperties facet) {
        this.state = state;
        this.facet = facet;
        this.roots = Collections.emptyList();
        this.aldorSdk = null;
    }

    @Nullable
    public static AldorModuleFacade facade(JpsModule module) {
        if (module.asTyped(JpsAldorModuleType.INSTANCE) == null) {
            return null;
        }
        return new AldorModuleFacade(module);
    }

    public boolean generateMakefile() {
        return false;
    }

    /** The directory from which make should be invoked */
    public File buildDirectory(File contentRoot, File sourceRoot, File sourceFile) {
        switch (facet.makeConvention()) {
            case Configured:
                return configuredBuildDirectory(contentRoot, sourceRoot, sourceFile);
            case Source:
                return sourceRoot;
            case Build:
                String rel = FileUtilRt.getRelativePath(contentRoot, sourceRoot);
                return Path.of(facet.outputDirectory(), rel).normalize().toFile();
            default:
                throw new IllegalStateException("Unknown build type: " + facet.makeConvention());
        }
    }

    private File configuredBuildDirectory(File contentRoot, File sourceRoot, File sourceFile) {
        File baseDir = contentRoot; // should be location of configure.ac
        return Path.of(facet.outputDirectory(), FileUtilRt.getRelativePath(contentRoot, sourceRoot)).toFile();
    }

    @NotNull
    @Contract(pure = true)
    public String targetName(@NotNull File sourceRoot, @NotNull File file) {
        switch (facet.makeConvention()) {
            case Configured:
                throw new IllegalStateException();
            case Source:
                String destFile = FileUtilRt.getNameWithoutExtension(file.getName()) + ".ao";
                String base = FileUtilRt.getRelativePath(sourceRoot, file.getParentFile());
                Path resolved = Path.of(facet.relativeOutputDirectory(), base, destFile);
                return resolved.normalize().toString();
            case Build:
            default:
                throw new IllegalStateException("Not implemented");
        }
    }

    @NotNull
    private String buildRelativeTargetName(@NotNull File file) {
        return trimExtension(file.getName()) + ".ao";
    }

    @NotNull
    private String sourceTargetName(File sourceRoot, @NotNull File file) {
        File fileDir = file.getParentFile();
        String relPath = FileUtilRt.getRelativePath(sourceRoot, fileDir);
        return Path.of(facet.relativeOutputDirectory(), relPath, FileUtilRt.getNameWithoutExtension(file.getName()) + ".ao").normalize().toString();
    }

    public boolean buildJavaComponents() {
        return facet.buildJavaComponents();
    }

    /**
     *
     * @deprecated Not for use in real code - add an accessing method if necessary
     */
    @VisibleForTesting
    @Deprecated
    public AldorFacetExtensionProperties facet() {
        return facet;
    }

    public List<JpsTypedModuleSourceRoot<AldorSourceRootProperties>> sourceRoots() {
        return roots;
    }

    @Override
    public String toString() {
        return "AldorModuleFacade{" +
                "relOutput=" + facet.relativeOutputDirectory() +
                ", make=" + facet.makeConvention() +
                ", aldor=" + facet.sdkName() +
                ", roots=" + roots.stream().map(r -> r.getUrl()).collect(Collectors.joining(",")) +
                '}';
    }

    public String sdkPath() {
        return aldorSdk;
    }

    public String javaSdkPath() {
        return facet.javaSdkName();
    }

    /*

   Example - IntellijSettings
        makefile is srcroot/'Makefile' in all cases.  It will run with -DbuildDir=..., and include 'builddir/intellij.mk'
        [Build dir & src root]
        (build, src) - build in 'build' directory
        (build, src/main/aldor) - build in build/src/main
        (build, src/main/aldor, src/test/aldor) - build in build/src/main & build/src/test
        (build, wibble/src) - build in build/wibble

        build target is relative path from source root to .as, s/.as/.ao
     */

}
