package aldor.build.module;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.aldor.AldorFacetType;
import aldor.build.facet.cfgroot.ConfigRootFacet;
import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.module.AldorFacetProperties;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static aldor.builder.jps.module.MakeConvention.Configured;

public class AldorModuleFacade implements SpadModuleFacade {
    private final Module module;
    private final AldorFacet facet;

    AldorModuleFacade(Module module) {
        this.module = module;
        this.facet = FacetManager.getInstance(module).getFacetByType(AldorFacetType.instance().getId());
    }

    public static boolean isAldorModule(Module module) {
        AldorFacet facet = AldorFacet.forModule(module);
        return facet != null;
    }

    public boolean hasJava() {
        // Maybe this needs configRoot & javaEnabled?
        return facet.getProperties().map(x -> x.buildJavaComponents()).orElse(false);
    }

    @TestOnly
    public AldorFacet facetForTesting() {
        return facet;
    }

    private AldorFacet facet() {
        return facet;
    }

    public Optional<AldorFacetProperties> properties() {
        return facet().getProperties();
    }

    public @Nullable File buildDirectory(VirtualFile file) {
        return buildDirectory(file.toNioPath().toFile());
    }

    // See AldorJpsModuleFacade (NB: May need to be adjusted to include sourceRoot -> file relative path)
    @Nullable
    public File buildDirectory(File sourceFile) {
        AldorFacetProperties props = facet.getProperties().orElse(null);
        if (props == null) {
            return null;
        }
        switch (props.makeConvention()) {
            case Configured:
                Optional<VirtualFile> configuredRootDir = configuredRootDirectory();
                Optional<String> configuredBuildDir = configuredBuildDirectory();
                if (configuredRootDir.isPresent() && sourceRootAsFile().isPresent() && configuredBuildDir.isPresent()) {
                    String relPath = FileUtil.getRelativePath(configuredRootDir.get().toNioPath().toFile(), sourceFile.getParentFile());
                    return Path.of(configuredBuildDir.get(), relPath).toFile();
                }
                else {
                    return null;
                }
            case None:
                return null;
            case Source:
                return new File(sourceFile.getParentFile(), props.relativeOutputDirectory());
            case Build:
                Optional<VirtualFile> sourceRoot = sourceRoot();
                if (sourceRoot.isPresent()) {
                    String rel = FileUtilRt.getRelativePath(new File(props.outputDirectory()), sourceRoot.get().toNioPath().toFile());
                    return Path.of(props.outputDirectory(), rel).normalize().toFile();
                }
                else {
                    return null;
                }
            default:
                throw new IllegalStateException("Unknown build type: " + props.makeConvention());
        }
    }

    private File configuredBuildDirectory(File contentRoot, File sourceRoot, File sourceFile) {
        return Path.of(facet.getProperties().get().outputDirectory(), FileUtilRt.getRelativePath(contentRoot, sourceRoot)).toFile();
    }

    public @Nullable File buildDirectory() {
        if (properties().isEmpty()) {
            return null;
        }
        if (isConfigured()) {
            Optional<String> buildDir = configuredBuildDirectory();
            Optional<File> buildDirFile = buildDir.map(bd -> new File(bd));
            var relDir = properties().get().relativeOutputDirectory();
            return buildDirFile.map(bdf -> new File(bdf, relDir)).orElse(null);
        }
        else {
            return facet().getProperties().map(x -> new File(x.outputDirectory())).orElse(null);
        }
    }

    private Optional<File> sourceRootAsFile() {
        return sourceRoot().map(x -> x.toNioPath().toFile());
    }

    private Optional<VirtualFile> sourceRoot() {
        var roots = ModuleRootManager.getInstance(module).getSourceRoots(AldorSourceRootType.INSTANCE);
        if (roots.size() != 1) {
            return Optional.empty();
        }
        else {
            return Optional.of(roots.get(0));
        }
    }

    private Optional<VirtualFile> contentRoot() {
        var roots = ModuleRootManager.getInstance(module).getContentRoots();
        if (roots.length != 1) {
            return Optional.empty();
        }
        else {
            return Optional.of(roots[0]);
        }
    }

    private Optional<VirtualFile> configuredRootDirectory() {
        if (contentRoot().isEmpty()) {
            return Optional.empty();
        }
        VirtualFile root = ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(contentRoot().get().getParent());
        return Optional.ofNullable(root);
    }

    @NotNull
    private Optional<String> configuredBuildDirectory() {
        assert isConfigured();
        if (contentRoot().isEmpty()) {
            return Optional.empty();
        }
        Module rootModule = ProjectRootManager.getInstance(module.getProject()).getFileIndex().getModuleForFile(contentRoot().get().getParent());
        if (rootModule == null) {
            return Optional.empty();
        }

        var configRootFacet = ConfigRootFacet.forModule(rootModule);
        if ((configRootFacet == null) || (configRootFacet.getConfiguration().getState() == null)) {
            return Optional.empty();
        }
        VirtualFile[] roots = ModuleRootManager.getInstance(rootModule).getContentRoots();
        if (roots.length != 1) {
            return Optional.empty();
        }
        VirtualFile root = ModuleRootManager.getInstance(rootModule).getContentRoots()[0];
        String buildDirText = configRootFacet.getConfiguration().getState().buildDirectory();

        if (buildDirText == null) {
            return Optional.empty();
        }
        File buildDir = new File(buildDirText);
        if (buildDir.isAbsolute()) {
            return Optional.of(buildDir.toString());
        }
        else {
            return Optional.of(root.findFileByRelativePath(buildDirText).toNioPath().toString());
        }
    }

    public static Optional<AldorModuleFacade> forModule(Module module) {
        return isAldorModule(module) ? Optional.of(new AldorModuleFacade(module)): Optional.empty();
    }

    @Override
    public boolean isConfigured() {
        return properties().map(p -> p.makeConvention() == Configured).orElse(false);
    }

    public List<Module> moduleDependencies() {
        List<Module> deps = new ArrayList<>();
        ModuleRootManager.getInstance(module).orderEntries().forEachModule(mod -> {if (isAldorModule(mod)) {deps.add(mod);} return true;});
        return deps;
    }
}
