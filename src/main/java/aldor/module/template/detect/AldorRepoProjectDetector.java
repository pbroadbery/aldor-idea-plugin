package aldor.module.template.detect;

import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.aldor.AldorFacetConfiguration;
import aldor.build.facet.aldor.AldorFacetConstants;
import aldor.build.facet.aldor.AldorFacetType;
import aldor.build.facet.cfgroot.ConfigRootFacet;
import aldor.build.facet.cfgroot.ConfigRootFacetConfiguration;
import aldor.build.facet.cfgroot.ConfigRootFacetType;
import aldor.build.module.AldorModuleType;
import aldor.builder.jps.AldorSourceRootType;
import aldor.builder.jps.module.AldorFacetProperties;
import aldor.builder.jps.module.ConfigRootFacetProperties;
import aldor.builder.jps.module.MakeConvention;
import aldor.util.Streams;
import com.google.common.collect.ImmutableMap;
import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.history.core.Paths;
import com.intellij.ide.util.importProject.ModuleDescriptor;
import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.importSources.DetectedContentRoot;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AldorRepoProjectDetector extends ProjectStructureDetector {
    private static final Logger LOG = Logger.getInstance(AldorRepoProjectDetector.class);
    private final Set<String> signatureRootPaths = Set.of(
            "aldor", "aldorug", "configure.ac", "lib/buildlib.am"
    );
    private final Set<String> parentRootPaths = Set.of("aldor", "README.md");
    private final Set<String> getSignatureBuildRootPaths = Set.of("Makefile", "config.log");

    @Override
    public @NotNull DirectoryProcessingResult detectRoots(@NotNull File dir, File @NotNull [] children, @NotNull File base, @NotNull List<DetectedProjectRoot> result) {
        boolean configRootMatch = signatureRootPaths.stream().allMatch(p -> new File(dir, p).exists());
        boolean descend = true;
        if (configRootMatch && parentCheck(dir.getParentFile(), base)) {
            result.add(new ConfigRootCandidate(dir));
            result.add(new ConfigRepoRootCandidate(dir.getParentFile()));
            var libraries = Arrays.stream(Objects.requireNonNull(new File(dir, "lib").listFiles()))
                    .filter(x -> x.isDirectory() && x.canExecute())
                    .toList();
            for (var lib: libraries) {
                DetectedContentRoot root = new DetectedContentRoot(lib, "AldorLib", AldorModuleType.instance());
                result.add(root);
            }
            descend = true;
        }

        boolean configBuildRootMatch = getSignatureBuildRootPaths.stream().allMatch(p -> new File(dir, p).exists());
        if (configBuildRootMatch) {
            result.add(new ConfigBuildRootCandidate(dir));
            descend = false;
            // Maybe try to create temporary roots for java stuff
        }
        return descend ? DirectoryProcessingResult.PROCESS_CHILDREN: DirectoryProcessingResult.SKIP_CHILDREN;
    }

    private boolean parentCheck(File parent, File base) {
        return FileUtil.isAncestor(base, parent, false)
                && parentRootPaths.stream().allMatch(p -> new File(parent, p).exists());
    }

    static boolean isConfigRoot(DetectedProjectRoot root) {
        return root instanceof ConfigRootCandidate;
    }

    static boolean isRepoRoot(DetectedProjectRoot root) {
        return root instanceof ConfigRepoRootCandidate;
    }

    static boolean isConfigBuildRoot(DetectedProjectRoot root) {
        return root instanceof ConfigBuildRootCandidate;
    }

    static <T extends DetectedProjectRoot> Optional<T> asRoot(Class<T> clzz, DetectedProjectRoot root) {
        return (clzz.isAssignableFrom(root.getClass())) ? Optional.of(clzz.cast(root)) : Optional.empty();
    }

    public interface BuilderFunctions {
        String uniqueName(File file, String name);
    }


    @Override
    public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
                                      @NotNull ProjectDescriptor projectDescriptor,
                                      @NotNull ProjectFromSourcesBuilder builder) {
        setupProjectStructure(roots, projectDescriptor, new BuilderFunctions() {
            @Override
            public String uniqueName(File file, String name) {
                return AldorRepoProjectDetector.this.uniqueName(builder, file, name);
            }
        });
    }

    public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
                                      @NotNull ProjectDescriptor projectDescriptor,
                                      @NotNull BuilderFunctions builderFunctions) {
        LOG.info("Setup project structure... Detected roots " + roots);
        List<ModuleDescriptor> modules = new ArrayList<>();
        Collection<DetectedProjectRoot> otherRoots = new ArrayList<>();
        Collection<ModuleDescriptor> libDescriptors = new ArrayList<>();

        for (DetectedProjectRoot root : roots) {
            if (root instanceof DetectedContentRoot contentRoot) {
                var parentRootMaybe = roots.stream()
                        .filter(x -> isConfigRoot(x))
                        .filter(x -> Paths.isParent(x.getDirectory().getAbsolutePath(), root.getDirectory().getAbsolutePath()))
                        .findFirst();
                if (parentRootMaybe.isPresent()) {
                    DetectedProjectRoot parentRoot = parentRootMaybe.get();
                    var descriptor = new ModuleDescriptor(root.getDirectory().getAbsoluteFile(), contentRoot.getModuleType(),
                            Collections.emptyList());
                    descriptor.setName(builderFunctions.uniqueName(root.getDirectory(), root.getDirectory().getName() + "-lib"));
                    libDescriptors.add(descriptor);
                    descriptor.addConfigurationUpdater(new ModuleBuilder.ModuleConfigurationUpdater() {
                        @Override
                        public void update(@NotNull Module module, @NotNull ModifiableRootModel rootModel) {
                            LOG.info("setupProjectStructure::LibUpdate: " + module.getName());
                            String relativePath = FileUtil.getRelativePath(parentRoot.getDirectory(), root.getDirectory());
                            var properties = AldorFacetProperties.newBuilder()
                                    .makeConvention(MakeConvention.Configured)
                                    .relativeOutputDirectory(relativePath)
                                    .java(AldorFacetProperties.WithJava.Enabled) // TODO: Build, java=false
                                    .build();
                            var facetManager = FacetManager.getInstance(module);
                            AldorFacetConfiguration configuration = new AldorFacetConfiguration();
                            configuration.loadState(properties);
                            AldorFacet facet = new AldorFacet(AldorFacetType.instance(), module, module.getName() + "(Facet)", configuration, null);
                            var mgr = facetManager.createModifiableModel();
                            mgr.addFacet(facet);
                            mgr.commit();
                            LOG.info("Created facet " + facet.getName());

                            ContentEntry entry = rootModel.getContentEntries()[0];
                            VirtualFile srcDir = Optional.ofNullable(entry.getFile()).map( f -> f.findChild("src")).orElse(null);
                            if ((srcDir != null) && srcDir.isDirectory()) {
                                LOG.info("setupProjectStructure::LibUpdate:srcDir: " +  srcDir);
                                entry.addSourceFolder(srcDir, AldorSourceRootType.INSTANCE);
                            }
                        }
                    });
                    modules.add(descriptor);
                }
            }
            else if (isConfigRoot(root)) {
                ModuleDescriptor descriptor = new ModuleDescriptor(root.getDirectory().getAbsoluteFile(), ModuleType.EMPTY, Collections.emptyList());
                descriptor.setName(builderFunctions.uniqueName(root.getDirectory(), root.getDirectory().getName()));
                descriptor.addConfigurationUpdater(new ModuleBuilder.ModuleConfigurationUpdater() {
                    @Override
                    public void update(@NotNull Module module, @NotNull ModifiableRootModel rootModel) {
                        LOG.info("setupProjectStructure::ConfigUpdate(Root):srcDir: " +  root.getDirectory());
                        var dir = root.getDirectory();
                        ConfigRootFacetProperties properties = ConfigRootFacetProperties.newBuilder()
                                .setBuildDirectory(new File(dir.getParentFile().getParentFile(), "build").toString())
                                .setInstallDirectory(new File(dir.getParentFile().getParentFile(), "opt").toString())
                                .setDefined(true)
                                .build();
                        ConfigRootFacetConfiguration configuration = new ConfigRootFacetConfiguration();
                        configuration.loadState(properties);
                        FacetManager facetManager = FacetManager.getInstance(module);
                        LOG.info("setupProjectStructure::ConfigUpdate(Root):creating facet: " +  module.getName());
                        ConfigRootFacet facet = facetManager.createFacet(ConfigRootFacetType.instance(),
                                root.getDirectory().getName() + "." + AldorFacetConstants.ROOT_FACET_NAME, configuration, null);
                        ModifiableFacetModel facetModel = facetManager.createModifiableModel();
                        facetModel.addFacet(facet);
                        facetModel.commit();
                    };
                });

                modules.add(descriptor);
            }
            else if (isRepoRoot(root)) {
                ModuleDescriptor descriptor = new ModuleDescriptor(root.getDirectory().getAbsoluteFile(), ModuleType.EMPTY, Collections.emptyList());
                descriptor.setName(builderFunctions.uniqueName(root.getDirectory(), "aldor-root"));
                //descriptor.addContentRoot(new File(root.getDirectory().getParentFile(), "build"));
                //descriptor.addContentRoot(new File(root.getDirectory().getParentFile(), "opt"));
                modules.add(descriptor);
            }
            else {
                otherRoots.add(root);
            }
        }
        for (var other: otherRoots) {
            if (other instanceof DetectedContentRoot) {
                modules.add(new ModuleDescriptor(other.getDirectory(), ((DetectedContentRoot)other).getModuleType(), Collections.emptyList()));
            }
        }

        addModuleDependencies(libDescriptors);

        LOG.info("Created " + modules.size() + " modules");
        projectDescriptor.setModules(modules);
    }

    // Hard to avoid, so just do it..
    // NB: Should add some kind of editor for this..
    private static Map<String, List<String>> dependencyForModule = ImmutableMap.<String, List<String>>builder()
            .put("axldem", List.of("axllib"))
            .put("algebra", List.of("aldor"))
            .put("types", List.of("aldor"))
            .build();

    private void addModuleDependencies(Collection<ModuleDescriptor> libDescriptors) {
        Map<String, ModuleDescriptor> modDescriptorForName = new HashMap<>();
        for (ModuleDescriptor desc: libDescriptors) {
            String rootName = desc.getContentRoots().iterator().next().getName();
            modDescriptorForName.put(rootName, desc);
        }
        for (var ent: dependencyForModule.entrySet()) {
            ModuleDescriptor lhs = modDescriptorForName.get(ent.getKey());
            if (lhs != null) {
                for (String other: ent.getValue()) {
                    if (modDescriptorForName.get(other) != null) {
                        lhs.addDependencyOn(modDescriptorForName.get(other));
                    }
                }
            }
        }
    }

    String uniqueName(ProjectFromSourcesBuilder builder, File directory, String baseName) {
        var existingNames = builder.getExistingModuleNames();
        String name = baseName;
        int index = 1;
        while (existingNames.contains(name) || moduleFileExists(directory, name)) {
            name = baseName + "-" + index;
            index++;
        }
        return name;
    }

    private boolean moduleFileExists(File directory, String name) {
        File file = new File(directory, name + ".iml");
        LOG.info("Checking for module file " + file + " exists: " + file.exists());
        return file.exists();
    }

    @Override
    public List<ModuleWizardStep> createWizardSteps(ProjectFromSourcesBuilder builder, ProjectDescriptor projectDescriptor, Icon stepIcon) {
        LOG.info("Creating new project: " + builder.getContext().isCreatingNewProject());
        LOG.info("Creating new project: " + builder.getProjectRoots(this));
        return List.of(new AldorConfigurationWizard(this, builder, projectDescriptor, stepIcon));
    }

    public void configure(ProjectFromSourcesBuilder builder,
                          DetectedRootFacetSettings rootSettings) {
        ProjectDescriptor projectDescriptor = builder.getProjectDescriptor(this);
        configure(projectDescriptor, builder.getProjectRoots(this), rootSettings);
    }

    public void configure(ProjectDescriptor projectDescriptor,
                          Collection<DetectedProjectRoot> roots,
                          DetectedRootFacetSettings rootSettings) {

        for (var rootCandidate : roots.stream().flatMap(Streams.filterAndCast(ConfigRootCandidate.class)).toList()) {
            Optional<ModuleDescriptor> rootModule = projectDescriptor.getModules().stream()
                    .filter(x -> x.getContentRoots().contains(rootCandidate.getDirectory()))
                    .findFirst();
            var settings = rootSettings.get(rootCandidate.getDirectory());
            if (settings == null) {
                LOG.info("updateDataModel::Missing settings for " + rootCandidate.getDirectory());
                LOG.info("updateDataModel::Missing settings for " + rootSettings.facetPropertiesForRoot().keySet());
                return;
            }
            LOG.info("updateDataModel::Fount module descriptor "+ rootModule);
            rootModule.ifPresent(moduleDescriptor -> moduleDescriptor.addConfigurationUpdater(new ModuleBuilder.ModuleConfigurationUpdater() {
                @Override
                public void update(@NotNull Module module, @NotNull ModifiableRootModel rootModel) {
                    LOG.info("updateDataModel::Checking module "+ module.getName() + " "+ moduleDescriptor.getName());
                    LOG.info("updateDataModel::New settings are "+ settings);

                    if (!module.getName().equals(moduleDescriptor.getName())) {
                        return;
                    }

                    FacetManager facetMgr = FacetManager.getInstance(module);
                    ModifiableFacetModel facetModel = facetMgr.createModifiableModel();
                    var properties = settings.asBuilder()
                            .setDefined(true)
                            .build();
                    @NotNull ConfigRootFacetConfiguration config = new ConfigRootFacetConfiguration();
                    config.loadState(properties);
                    var facet = facetMgr.getFacetByType(ConfigRootFacetType.TYPE_ID);
                    if (facet != null) {
                        LOG.info("updateDataModel::Removing facet "+ facet);
                        facetModel.removeFacet(facet);
                    }
                    var rootFacet = facetMgr.createFacet(ConfigRootFacetType.instance(), module.getName() + " [ConfigRoot]", config, null);
                    facetModel.addFacet(rootFacet);
                    LOG.info("updateDataModel::Adding facet "+ facet);
                    facetModel.commit();
                }
            }));

        }
    }
}
