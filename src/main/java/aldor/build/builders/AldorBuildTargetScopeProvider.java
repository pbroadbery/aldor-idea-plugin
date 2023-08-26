package aldor.build.builders;

import aldor.build.module.AldorModuleFacade;
import aldor.builder.AldorTargetIds;
import aldor.builder.jps.AldorSourceRootType;
import aldor.file.AldorFileType;
import com.intellij.compiler.impl.BuildTargetScopeProvider;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static aldor.builder.AldorBuildConstants.ALDOR_FILE_TARGET;
import static aldor.builder.AldorBuildConstants.ALDOR_JAR_TARGET;
import static aldor.builder.AldorBuildConstants.PHONY_ALDOR_FILE_TARGET;

/**
 * Maps a change list into a bunch of stuff to do for the compiler
 */
public class AldorBuildTargetScopeProvider extends BuildTargetScopeProvider {
    private static final Logger LOG = Logger.getInstance(AldorBuildTargetScopeProvider.class);

    public static List<TargetTypeBuildScope> scopeForOneFile(Project project, VirtualFile file) {
        var projectFileIndex = ProjectFileIndex.getInstance(project);
        final Collection<Pair<String, String>> targetIds = new ArrayList<>();

        Module module = ProjectFileIndex.getInstance(project).getModuleForFile(file);
        Optional<AldorModuleFacade> facade = AldorModuleFacade.forModule(module);
        if (facade.isEmpty()) {
            return Collections.emptyList();
        }
        collectIds(targetIds, projectFileIndex, file);

        var ids = reformatIds(true, targetIds);
        if (facade.get().isConfigured()) {
            ids.add(TargetTypeBuildScope.newBuilder()
                    .setForceBuild(false)
                    .setTypeId("Script")
                    .setAllTargets(true)
                    .build());
        }
        return ids;
    }

    @NotNull
    @Override
    public List<TargetTypeBuildScope> getBuildTargetScopes(
            @NotNull final CompileScope baseScope,
            @NotNull final Project project,
            final boolean forceBuild) {
        LOG.info("get build targets - forceBuild: " + forceBuild);
        LOG.info("get build targets - scope: " + baseScope);
        LOG.info("get build targets - modules: " + Arrays.asList(baseScope.getAffectedModules()));
        LOG.info("get build targets - files: " + Arrays.asList(baseScope.getFiles(AldorFileType.INSTANCE, true)));

        // produce a list of target ids
        final Collection<Pair<String, String>> targetIds = new ArrayList<>();
        VirtualFile[] files = baseScope.getFiles(AldorFileType.INSTANCE, true); // TODO: Account for 'excluded'

        List<TargetTypeBuildScope> targetTypeBuildScopes = new ArrayList<>();
        var projectFileIndex = ProjectFileIndex.getInstance(project);
        if (files.length == 0) {
            return Collections.emptyList();
        }

        for (VirtualFile file: files) {
            collectIds(targetIds, projectFileIndex, file);
        }

        targetTypeBuildScopes.addAll(reformatIds(forceBuild, targetIds));
        boolean foundConfigured = false;
        for (Module module: baseScope.getAffectedModules()) {
            Optional<AldorModuleFacade> facadeMaybe = AldorModuleFacade.forModule(module);
            if (facadeMaybe.map(x -> x.isConfigured()).orElse(false)) {
                foundConfigured = true;
            }
        }
        for (Module module : baseScope.getAffectedModules()) {
            Optional<AldorModuleFacade> facadeMaybe = AldorModuleFacade.forModule(module);
            if (facadeMaybe.isPresent()) {
                AldorModuleFacade facade = facadeMaybe.get();
                if (!facade.isConfigured()) {
                    if (facade.hasJava()) {
                        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
                        for (VirtualFile vf : sourceRoots) {
                            LOG.info("Found source root: " + vf.getCanonicalPath());
                            TargetTypeBuildScope build = TargetTypeBuildScope.newBuilder()
                                    .setTypeId(ALDOR_JAR_TARGET)
                                    .setForceBuild(forceBuild)
                                    .addTargetId(AldorTargetIds.aldorJarTargetId(vf.getPath()))
                                    .build();
                            targetTypeBuildScopes.add(build);
                        }
                    }
                }
            }
        }

        for (Module module : baseScope.getAffectedModules()) {
            Optional<AldorModuleFacade> facadeMaybe = AldorModuleFacade.forModule(module).filter(x -> x.isConfigured());
            if (facadeMaybe.isPresent()) {
                AldorModuleFacade facade = facadeMaybe.get();
                TargetTypeBuildScope build = TargetTypeBuildScope.newBuilder()
                        .setTypeId(PHONY_ALDOR_FILE_TARGET)
                        .setForceBuild(forceBuild)
                        .addTargetId(configuredPhonyTargetModuleId(module))
                        .build();
                targetTypeBuildScopes.add(build);
            }
        }
        if (foundConfigured) {
            TargetTypeBuildScope ttscope = TargetTypeBuildScope.newBuilder()
                    .setForceBuild(forceBuild)
                    .setTypeId("Script")
                    .setAllTargets(true)
                    .build();
            targetTypeBuildScopes.add(ttscope);
        }
        return targetTypeBuildScopes;
    }

    private static List<TargetTypeBuildScope> reformatIds(boolean forceBuild, Collection<Pair<String, String>> targetIds) {
        List<TargetTypeBuildScope> targetTypeBuildScopes = new ArrayList<>();
        for (String type: List.of(ALDOR_FILE_TARGET, PHONY_ALDOR_FILE_TARGET)) {
            TargetTypeBuildScope req = TargetTypeBuildScope.newBuilder()
                    .setTypeId(type)
                    .setForceBuild(forceBuild)
                    .addAllTargetId(targetIds.stream()
                            .filter(x -> x.first.equals(type))
                            .map(x -> x.second)
                            .collect(Collectors.toList()))
                    .build();

            if (!req.getTargetIdList().isEmpty()) {
                targetTypeBuildScopes.add(req);
            }
        }
        return targetTypeBuildScopes;
    }

    private static void collectIds(Collection<Pair<String, String>> targetIds, ProjectFileIndex projectFileIndex, VirtualFile file) {
        Module module = projectFileIndex.getModuleForFile(file);
        Optional<AldorModuleFacade> facadeMaybe = AldorModuleFacade.forModule(module);
        if (facadeMaybe.isPresent()) {
            AldorModuleFacade facade = facadeMaybe.get();
            if (facade.isConfigured()) {
                configuredPhonyTargetId(module, file)
                        .ifPresent(id -> targetIds.add(Pair.create(PHONY_ALDOR_FILE_TARGET, id)));
            } else {
                targetIds.add(Pair.create(ALDOR_FILE_TARGET, file.getCanonicalPath()));
            }
        }
    }

    private static Optional<String> configuredPhonyTargetId(Module module, VirtualFile file) {
        var roots = ModuleRootManager.getInstance(module).getSourceRoots(AldorSourceRootType.INSTANCE);
        if (roots.size() != 1) {
            return Optional.empty();
        }
        else {
            var theRoot = roots.get(0);
            String relPath = FileUtil.getRelativePath(theRoot.toNioPath().toFile(), file.toNioPath().toFile());
            return Optional.of(String.format("{%s}-{%s}", module.getName(), relPath));
        }
    }

    private static String configuredPhonyTargetModuleId(Module module) {
        return String.format("{%s}", module.getName());
    }
}
