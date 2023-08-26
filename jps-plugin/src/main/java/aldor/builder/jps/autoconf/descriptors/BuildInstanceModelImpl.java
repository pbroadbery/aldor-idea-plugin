package aldor.builder.jps.autoconf.descriptors;

import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.module.ConfigRootFacetProperties;
import aldor.builder.jps.module.JpsAldorFacetExtension;
import aldor.builder.jps.util.Sx;
import aldor.util.FileFilterAldorUtils;
import aldor.util.InstanceCounter;
import aldor.util.SxForm;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static aldor.util.SxFormUtils.file;
import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;
import static aldor.util.SxFormUtils.number;
import static aldor.util.SxFormUtils.tagged;

public class BuildInstanceModelImpl implements BuildInstanceModel {
    private static final Logger LOG = Logger.getInstance(BuildInstanceModelImpl.class);
    private static final File[] EMPTY_FILES = new File[0];
    private final int instanceId = InstanceCounter.instance().next(BuildInstanceModelImpl.class);
    private final BuildStaticModel staticModel;
    private final JpsModel jpsModel;
    private final File rootDirectory;
    private final ConfigRootFacetProperties facetProperties;
    private final TargetCollection targetCollection;
    private final JpsModule jpsModule;

    public BuildInstanceModelImpl(BuildStaticModel staticModel,
                                  JpsModel jpsModel,
                                  JpsModule jpsModule,
                                  File rootDirectory,
                                  ConfigRootFacetProperties facetProperties) {
        this.staticModel = staticModel;
        this.jpsModel = jpsModel;
        this.jpsModule = jpsModule;
        this.rootDirectory = rootDirectory;
        this.facetProperties = facetProperties;
        this.targetCollection = createTargetCollection();
    }

    public JpsModel jpsModel() {
        return jpsModel;
    }
    @Override
    public JpsModule jpsModule() {
        return jpsModule;
    }

    public BuildStaticModel staticModel() {
        return staticModel;
    }

    @Override
    public File rootDirectory() {
        return rootDirectory;
    }

    @Override
    public File targetDirectory() {
        // FIXME: Needs to be relative path aware
        Path buildPath = FileSystems.getDefault().getPath(facetProperties.buildDirectory());
        if (buildPath.isAbsolute()) {
            return buildPath.toFile();
        }
        else {
            return new File(rootDirectory(), facetProperties.buildDirectory()).toPath().normalize().toFile();
        }
    }

    @Override
    public List<String> configureOptions() {
        return facetProperties.configureArguments();
    }

    @Override @NotNull
    public Collection<ScriptTargetDescriptor> allScriptTargets() {
        return targetCollection.allScriptTargets();
    }

    @Override @NotNull
    public Collection<PhonyTargetDescriptor> allPhonyTargets() {
        return targetCollection.allPhonyTargets();
    }

    @Override
    public Collection<AbstractTargetDescriptor> dependencies(AbstractTargetDescriptor descriptor) {
        return targetCollection.dependencies(descriptor);
    }

    @Override
    @NotNull
    public SxForm sxForm() {
        return list()
                .add(name("BuildInstanceModel"))
                .add(tagged()
                        .with("instanceId", number(instanceId))
                        .with("rootDirectory", file(rootDirectory()))
                        .with("targetCollection", targetCollection.sxForm()));
    }

    private ScriptTargetDescriptor createTarget(ScriptType type) {
        return new ScriptTargetDescriptor(type, this.rootDirectory, this.targetDirectory());
    }

    @Override
    public boolean matchesJpsModel(JpsModel model) {
        //noinspection ObjectEquality
        return this.jpsModel == model;
    }

    private TargetCollection createTargetCollection() {
        LOG.info("Creating targets for " + rootDirectory + " building to " + targetDirectory());
        TargetCollection targetCollection = new TargetCollection();
        buildBaseTargetCollection(targetCollection);
        buildModuleTargetCollection(targetCollection);
        buildPerFileTargetCollection(targetCollection);
        return targetCollection;
    }

    void buildPerFileTargetCollection(TargetCollection targetCollection) {
        var modules = jpsModel.getProject().getModules().stream()
                .map(mod -> Optional.ofNullable(JpsAldorFacetExtension.getExtension(mod)).map(f -> Pair.pair(mod, f)))
                .flatMap(o -> o.stream())
                .filter(pair -> isLocal(pair.getFirst()))
                .toList();
        for (Pair<JpsModule, JpsAldorFacetExtension> pair : modules) {
            JpsModule module = pair.getFirst();
            Optional<JpsModuleSourceRoot> sourceRootMaybe = module.getSourceRoots().stream().filter(x -> AldorSourceRootType.isMainInstance(x.getRootType())).findFirst();
            if (sourceRootMaybe.isEmpty()) {
                LOG.warn("No aldor source root for module " + module.getName());
                continue;
            }
            JpsModuleSourceRoot sourceRoot = sourceRootMaybe.get();
            var sourceRootFile = sourceRoot.getFile();
            Map<Path, ScriptTargetDescriptor> subdirs = new HashMap<>();
            try (var files = Files.find(sourceRootFile.toPath(), 100, BuildInstanceModelImpl::isAldorFile)) {
                files.forEach( path -> {
                    try {
                        var relPath = FileUtilRt.getRelativePath(sourceRootFile, path.toFile());
                        File asFile = path.toFile();
                        String simpleName = String.format("{%s}-{%s}", module.getName(), relPath);
                        ScriptTargetDescriptor tgt = createBuildAbnDescriptor(asFile, relPath);
                        targetCollection.add(tgt);

                        PhonyTargetDescriptor phonyTgt = new PhonyTargetDescriptor(simpleName, "Build " + path.getFileName().toString() + " in " + module.getName());
                        targetCollection.add(phonyTgt);
                        targetCollection.addDependency(phonyTgt, tgt);

                        targetCollection.addDependency(tgt, targetCollection.libTarget());
                        ScriptTargetDescriptor prereqDescriptor = findPrerequisiteDescriptor(targetCollection, sourceRoot.getPath(), subdirs, path);
                        targetCollection.addDependency(tgt, prereqDescriptor);
                    }
                    catch (RuntimeException e) {
                        LOG.error("Failed to process " + path, e);
                    }
                });
            } catch (IOException e) {
                LOG.error("While looking for files in " + sourceRootFile.toPath(), e);
            }
        }
    }

    private ScriptTargetDescriptor findPrerequisiteDescriptor(TargetCollection targetCollection, Path sourceRoot, Map<Path, ScriptTargetDescriptor> targets, Path path) {
        if (targets.containsKey(path)) {
            return targets.get(path);
        }
        Path parentPath = path.getParent();
        String pathToSourceDir = FileUtilRt.getRelativePath(sourceRoot.toFile(), parentPath.toFile());
        String relativePart = FileUtilRt.getRelativePath(rootDirectory, sourceRoot.toFile());
        ScriptTargetDescriptor tgt = createTarget(ScriptType.makeTarget(relativePart, "prereq-" + pathToSourceDir));
        ScriptTargetDescriptor.RootDescriptor rootDescriptor = new ScriptTargetDescriptor.RootDescriptor("root", new SourceRootPattern(sourceRoot.toFile(), FileFilterAldorUtils.withExtension("as"), Collections.emptySet(), true));
        tgt.addRootDescriptor(rootDescriptor);
        targetCollection.add(tgt);
        targetCollection.addDependency(tgt, targetCollection.libTarget());
        targets.put(path, tgt);
        return tgt;
    }

    @NotNull
    private ScriptTargetDescriptor createBuildAbnDescriptor(File dirFile, String baseName) {
        String relativePart = FileUtilRt.getRelativePath(rootDirectory, dirFile);
        File subDirectory = new File(relativePart).getParentFile();
        String base = FileUtilRt.getNameWithoutExtension(dirFile.getName());
        ScriptTargetDescriptor tgt = createTarget(ScriptType.makeTarget(subDirectory.toString(), base + ".abn"));
        ScriptTargetDescriptor.RootDescriptor rootDescriptor = new ScriptTargetDescriptor.RootDescriptor("root", new SourceRootPattern(dirFile, FileFilterAldorUtils.withExtension("as"), Collections.emptySet(), true));
        tgt.addRootDescriptor(rootDescriptor);
        LOG.info("Adding target " + tgt.sxForm());
        return tgt;
    }

    @VisibleForTesting
    public static boolean isAldorFile(Path path, BasicFileAttributes attrs) {
        return attrs.isRegularFile() && FileUtilRt.extensionEquals(path.toString(), "as");
    }

    // Expect a module per aldor directory
    private void buildModuleTargetCollection(TargetCollection targetCollection) {
        var modules = jpsModel.getProject().getModules().stream()
                .map(mod -> Optional.ofNullable(JpsAldorFacetExtension.getExtension(mod)).map(f -> Pair.pair(mod, f)))
                .flatMap(o -> o.stream())
                .filter(pair -> isLocal(pair.getFirst()))
                .toList();
        Collection<AbstractTargetDescriptor> tgts = new ArrayList<>();
        for (Pair<JpsModule, JpsAldorFacetExtension> pair: modules) {
            JpsModule module = pair.getFirst();
            Optional<JpsModuleSourceRoot> sourceRoot = module.getSourceRoots().stream().filter(x -> AldorSourceRootType.isMainInstance(x.getRootType())).findFirst();
            if (sourceRoot.isEmpty()) {
                LOG.warn("No aldor source root for module " + module.getName());
            }
            else {
                ScriptTargetDescriptor tgt = createTarget(ScriptType.makeTarget(FileUtilRt.getRelativePath(rootDirectory, sourceRoot.get().getFile()), "all"));
                tgt.addRootDescriptor(new ScriptTargetDescriptor.RootDescriptor("all", new SourceRootPattern(sourceRoot.get().getFile(), FileFilterAldorUtils.all(), Collections.emptySet(), true)));
                targetCollection.add(tgt);
                targetCollection.addDependency(tgt, targetCollection.libTarget());
                tgts.add(tgt);

                String simpleName = String.format("{%s}", module.getName());
                PhonyTargetDescriptor phonyTgt = new PhonyTargetDescriptor(simpleName, "Build Module" + module.getName());
                targetCollection.add(phonyTgt);
                targetCollection.addDependency(phonyTgt, tgt);
                tgts.add(phonyTgt);

                //List<JpsModuleDependency> moduleDependencies = module.getDependenciesList().getDependencies().stream().flatMap(Streams.filterAndCast(JpsModuleDependency.class)).toList();

            }
        }
        tgts.forEach(tgtx -> LOG.info("Created target " + tgtx.sxForm().asSExpression()));
    }



    // Is this module part of this buildInstance
    private boolean isLocal(JpsModule module) {
        return true;
    }

    // Top level - autogen, configure, non-aldor source modules
    private void buildBaseTargetCollection(TargetCollection targetCollection) {
        var autogen = targetCollection.add(createTarget(ScriptType.Autogen));
        var configure = targetCollection.add(createTarget(ScriptType.Configure));
        // It would be nice to infer this stuff from makefiles
        var subcmd_all = targetCollection.add(createTarget(ScriptType.makeTarget("aldor/subcmd", "all")));
        var tools_all = targetCollection.add(createTarget(ScriptType.makeTarget("aldor/tools", "all")));
        var src_all = targetCollection.add(createTarget(ScriptType.makeTarget("aldor/src", "all")));
        var lib_all = targetCollection.add(createTarget(ScriptType.makeTarget("aldor/lib", "all")));

        targetCollection.addDependency(configure, autogen);
        targetCollection.addDependency(tools_all, subcmd_all);
        targetCollection.addDependency(src_all, tools_all);
        targetCollection.addDependency(lib_all, src_all);

        targetCollection.libTarget(lib_all);

        for (var tt: List.of(subcmd_all, tools_all, src_all, lib_all)) {
            targetCollection.addDependency(tt, configure);
        }

        // Source Roots
        Sx.FileFilter filter = FileFilterAldorUtils.dirWithExtension("m4", "m4)").or(FileFilterAldorUtils.only(new File(rootDirectory, "configure.ac")));
        autogen.addRootDescriptor(new ScriptTargetDescriptor.RootDescriptor(autogen.id(), new SourceRootPattern(rootDirectory, filter, Collections.emptySet(), true)));

        Set<File> subdirectories = Arrays.stream(Optional.ofNullable(rootDirectory.listFiles()).orElse(EMPTY_FILES)).filter(x -> x.isDirectory()).collect(Collectors.toSet());
        configure.addRootDescriptor(new ScriptTargetDescriptor.RootDescriptor(configure.id(), new SourceRootPattern(rootDirectory, FileFilterAldorUtils.only(new File(rootDirectory,"configure")), subdirectories, true)));

        PhonyTargetDescriptor phonyTgt = new PhonyTargetDescriptor("AldorRuntime", "Create aldor runtime");
        targetCollection.add(phonyTgt);
        targetCollection.runtimeTarget(phonyTgt);
        for (var tt: List.of(subcmd_all, tools_all, src_all, lib_all)) {
            Set<File> localSubdirectories = Arrays.stream(Optional.ofNullable(rootDirectory.listFiles()).orElse(EMPTY_FILES)).filter(x -> x.isDirectory()).collect(Collectors.toSet());
            // FIXME : 'all()' is possibly too many
            tt.addRootDescriptor(new ScriptTargetDescriptor.RootDescriptor(tt.id(), new SourceRootPattern(new File(rootDirectory, tt.subdirectory()), FileFilterAldorUtils.all(), localSubdirectories, true)));
        }

        for (var tt: List.of(subcmd_all, tools_all, src_all, lib_all)) {
            targetCollection.addDependency(phonyTgt, tt);
        }
        // Output roots
        autogen.addOutputRoot(rootDirectory);
        configure.addOutputRoot(targetDirectory());
        for (var tt: List.of(subcmd_all, tools_all, src_all, lib_all)) {
            tt.addOutputRoot(new File(targetDirectory(), tt.scriptType().subdirectory()));
        }

        PhonyTargetDescriptor subcmdPhony = new PhonyTargetDescriptor("just-subcmd", "Just the subcmd directory");
        targetCollection.add(subcmdPhony);
        targetCollection.addDependency(subcmdPhony, subcmd_all);

    }

    @Override
    public void close() {

    }


}
