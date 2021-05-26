package aldor.build.builders;

import aldor.build.facet.SpadFacet;
import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.aldor.AldorFacetType;
import aldor.build.module.AldorModuleType;
import aldor.builder.AldorTargetIds;
import aldor.builder.jps.SpadFacetProperties;
import aldor.builder.jps.module.AldorModuleFacade;
import aldor.file.AldorFileType;
import aldor.file.SpadFileType;
import com.intellij.compiler.impl.BuildTargetScopeProvider;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static aldor.builder.AldorBuildConstants.ALDOR_FILE_TARGET;
import static aldor.builder.AldorBuildConstants.ALDOR_JAR_TARGET;

/**
 * Maps a change list into a bunch of stuff to do for the compiler
 */
public class AldorBuildTargetScopeProvider extends BuildTargetScopeProvider {
    private static final Logger LOG = Logger.getInstance(AldorBuildTargetScopeProvider.class);

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
        final Collection<String> targetIds = new ArrayList<>();
        VirtualFile[] files = baseScope.getFiles(AldorFileType.INSTANCE, false); // TODO: Account for 'excluded'
        for (VirtualFile file: files) {
            targetIds.add(AldorTargetIds.aldorFileTargetId(file.getPath()));
        }

        List<TargetTypeBuildScope> targetTypeBuildScopes = new ArrayList<>();

        if (!targetIds.isEmpty()) {
            TargetTypeBuildScope req = TargetTypeBuildScope.newBuilder()
                    .setTypeId(ALDOR_FILE_TARGET)
                    .setForceBuild(forceBuild)
                    .addAllTargetId(targetIds)
                    .build();
            targetTypeBuildScopes.add(req);
        }

        Arrays.stream(baseScope.getAffectedModules())
                .filter(AldorModuleType.instance()::is)
                .filter(module -> shouldCreateJarFile(module))
                .map(module -> ModuleRootManager.getInstance(module).getSourceRoots())
                .flatMap(Arrays::stream)
                .peek(vf -> LOG.info("Found source root: " + vf.getCanonicalPath()))
                .map(root -> (TargetTypeBuildScope) TargetTypeBuildScope.newBuilder()
                        .setTypeId(ALDOR_JAR_TARGET)
                        .setForceBuild(forceBuild)
                        .addTargetId(AldorTargetIds.aldorJarTargetId(root.getPath()))
                        .build())
                .forEach(targetTypeBuildScopes::add);

        targetTypeBuildScopes.forEach(scope -> LOG.info("get build targets: --> " + scope.getTypeId() + " "+ scope.getTargetIdList()));

        return targetTypeBuildScopes;
    }

    @Nullable
    private boolean shouldCreateJarFile(com.intellij.openapi.module.Module module) {
        Optional<AldorFacet> facetMaybe = AldorFacetType.instance().facetIfPresent(module);
        return facetMaybe.flatMap(x -> x.getProperties()).map(x -> x.buildJavaComponents()).orElse(false);
    }
}
