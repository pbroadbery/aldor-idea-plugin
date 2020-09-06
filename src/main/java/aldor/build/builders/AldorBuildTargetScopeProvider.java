package aldor.build.builders;

import aldor.build.module.AldorModuleType;
import aldor.builder.AldorTargetIds;
import aldor.file.AldorFileType;
import com.intellij.compiler.impl.BuildTargetScopeProvider;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        LOG.info("get build targets - modules: " + Arrays.asList(baseScope.getAffectedModules()));
        LOG.info("get build targets - files: " + Arrays.asList(baseScope.getFiles(AldorFileType.INSTANCE, false)));

        // Gather the target IDs (module names) of the target modules.
        final Collection<String> targetIds = new ArrayList<>();
        VirtualFile[] files = baseScope.getFiles(AldorFileType.INSTANCE, false);

        for (VirtualFile file: files) {
            targetIds.add(AldorTargetIds.aldorFileTargetId(file.getPath()));
        }
        TargetTypeBuildScope req = TargetTypeBuildScope.newBuilder()
                .setTypeId(ALDOR_FILE_TARGET)
                .setForceBuild(forceBuild)
                .addAllTargetId(targetIds)
                .build();

        List<TargetTypeBuildScope> targetTypeBuildScopes = new ArrayList<>();
        Arrays.stream(baseScope.getAffectedModules())
                .filter(AldorModuleType.instance()::is)
                .map(module -> ModuleRootManager.getInstance(module).getSourceRoots())
                .flatMap(roots -> Arrays.stream(roots))
                .peek(vf -> LOG.info("Found source root: " + vf.getCanonicalPath()))
                .map(root -> TargetTypeBuildScope.newBuilder()
                        .setTypeId(ALDOR_JAR_TARGET)
                        .setForceBuild(forceBuild)
                        .addTargetId(AldorTargetIds.aldorJarTargetId(root.getPath()))
                        .build())
                .forEach(targetTypeBuildScopes::add);
        targetTypeBuildScopes.add(req);

        LOG.info("get build targets: --> " + targetTypeBuildScopes);
        return targetTypeBuildScopes;
    }

}
