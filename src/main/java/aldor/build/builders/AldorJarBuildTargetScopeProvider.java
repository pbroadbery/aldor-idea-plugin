package aldor.build.builders;

import aldor.builder.AldorTargetIds;
import com.intellij.compiler.impl.BuildTargetScopeProvider;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static aldor.builder.AldorBuildConstants.ALDOR_JAR_TARGET;

public class AldorJarBuildTargetScopeProvider extends BuildTargetScopeProvider {
    private static final Logger LOG = Logger.getInstance(AldorJarBuildTargetScopeProvider.class);

    @NotNull
    @Override
    public List<TargetTypeBuildScope> getBuildTargetScopes(
            @NotNull final CompileScope baseScope,
            @NotNull final Project project,
            final boolean forceBuild) {
        LOG.info("Jar scope provider: Scope type: " + baseScope.getClass().getSimpleName());
        if (!(baseScope instanceof AldorJarOnlyScope)) {
            return Collections.emptyList();
        }

        AldorJarOnlyScope scope = (AldorJarOnlyScope) baseScope;
        List<VirtualFile> sourceRoots = allSourceRoots(scope);

        List<String> ids = sourceRoots.stream().map(root -> AldorTargetIds.aldorJarTargetId(root.getCanonicalPath())).collect(Collectors.toList());

        TargetTypeBuildScope req = TargetTypeBuildScope
                .newBuilder()
                .setTypeId(ALDOR_JAR_TARGET)
                .setForceBuild(forceBuild)
                .addAllTargetId(ids).build();
        LOG.info("Adding " + req + " to build");
        return Collections.singletonList(req);
    }

    List<VirtualFile> allSourceRoots(AldorJarOnlyScope scope) {
        if (scope.isAllRoots()) {
            return Arrays.asList(ModuleRootManager.getInstance(scope.module()).getSourceRoots());
        }
        else {
            return Arrays.asList(scope.sourceRoots());
        }
    }

}
