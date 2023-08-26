package aldor.build;

import aldor.build.module.AldorModuleType;
import com.google.common.collect.Streams;
import com.intellij.ProjectTopics;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.Function;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.workspaceModel.ide.WorkspaceModelChangeListener;
import com.intellij.workspaceModel.ide.WorkspaceModelTopics;
import com.intellij.workspaceModel.storage.VersionedStorageChange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AldorCompilationStartupActivity implements StartupActivity, DumbAware {
    private static final Logger LOG = Logger.getInstance(AldorCompilationStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("Starting aldor plugin - disabling compile validation");
        CompilerManager compilerManager = CompilerManager.getInstance(project);
        compilerManager.setValidationEnabled(AldorModuleType.instance(), false);

        final MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void beforeRootsChange(@NotNull ModuleRootEvent event) {
                LOG.info("Pre-Root event: " + event.getProject().getName() + " model change: " + event.isCausedByWorkspaceModelChangesOnly());
            }

            @Override
            public void rootsChanged(@NotNull ModuleRootEvent event) {
                LOG.info("Root event: " + event.getProject().getName() + " model change: " + event.isCausedByWorkspaceModelChangesOnly());
                LOG.info("Root event: " + event);
            }

        });
        connection.subscribe(ProjectTopics.MODULES, new ModuleListener() {
            @Override
            public void modulesAdded(@NotNull Project project, @NotNull List<? extends Module> modules) {
                LOG.info("Module added: " + modules.stream().map(x -> x.getName()).toList());
            }

            @Override
            public void moduleRemoved(@NotNull Project xproject, @NotNull Module module) {
                LOG.info("ModuleRemoved: " + module.getName());
            }
            @Override
            public void modulesRenamed(@NotNull Project xproject,
                                       @NotNull List<? extends Module> modules,
                                       @NotNull Function<? super Module, String> oldNameProvider) {
                LOG.info("Renamed " + xproject.getName() + " - count: " + modules.size());
                for (Module module: modules) {
                    LOG.info("Renamed " + oldNameProvider.fun(module) + " --> " + module.getName());
                }
            }
        });
        connection.subscribe(WorkspaceModelTopics.CHANGED, new WorkspaceModelChangeListener() {
            @Override
            public void beforeChanged(@NotNull VersionedStorageChange event) {
                LOG.info("BeforeVersionedStorageChangeEvent: " + Streams.stream(event.getAllChanges().iterator()).toList());
                for (var e: Streams.stream(event.getAllChanges().iterator()).toList()) {
                    LOG.info("event: " + e.getClass() + " " + e.getNewEntity());
                }
            }

            @Override
            public void changed(@NotNull VersionedStorageChange event) {
                LOG.info("VersionedStorageChangeEvent: " + Streams.stream(event.getAllChanges().iterator()).toList());
                for (var e: Streams.stream(event.getAllChanges().iterator()).toList()) {
                    LOG.info("event: " + e.getClass() + " " + e.getNewEntity());
                }
            }
        });
        connection.subscribe(WorkspaceModelTopics.UNLOADED_ENTITIES_CHANGED, new WorkspaceModelChangeListener() {
            @Override
            public void beforeChanged(@NotNull VersionedStorageChange event) {
                LOG.info("Unloaded::BeforeVersionedStorageChangeEvent: " + Streams.stream(event.getAllChanges().iterator()).toList());
                for (var e: Streams.stream(event.getAllChanges().iterator()).toList()) {
                    LOG.info("event: " + e.getClass() + " " + e.getNewEntity());
                }
            }

            @Override
            public void changed(@NotNull VersionedStorageChange event) {
                LOG.info("Unloaded::VersionedStorageChangeEvent: " + Streams.stream(event.getAllChanges().iterator()).toList());
                for (var e: Streams.stream(event.getAllChanges().iterator()).toList()) {
                    LOG.info("event: " + e.getClass() + " " + e.getNewEntity());
                }
            }
        });
    }
}
