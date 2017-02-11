package aldor.build.module;

import aldor.editor.DefaultNavigator;
import aldor.parser.NavigatorFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static com.intellij.AppTopics.FILE_DOCUMENT_SYNC;

/**
 * Component that looks for aldor file updates, and if it spots them notifies the annotation manager.
 */
public class AldorApplicationComponent implements ApplicationComponent {
    private static final Logger LOG = Logger.getInstance(AldorApplicationComponent.class);
    private static final String NAME = "AldorFileWatcher";

    @Override
    public void initComponent() {
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = bus.connect();
        connection.subscribe(FILE_DOCUMENT_SYNC, new SaveActionProcessor());

        initialiseComponents();

    }

    private void initialiseComponents() {
        NavigatorFactory.registerDefaultNavigator(new DefaultNavigator());
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return NAME;
    }

    private class SaveActionProcessor extends FileDocumentManagerAdapter {
        @Override
        public void beforeDocumentSaving(@NotNull Document document) {
            Collection<PsiFile> psiFiles = new ArrayList<>();
            for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
                if (isPsiFileInProject(project, psiFile)) {
                    psiFiles.add(psiFile);
                }
            }

            for (PsiFile psiFile: psiFiles) {
                Project project = psiFile.getProject();
                Module module = ProjectFileIndex.getInstance(project).getModuleForFile(psiFile.getVirtualFile());
                if ((module == null) || !ModuleType.get(module).equals(AldorModuleType.instance())) {
                    continue;
                }
                Optional<AnnotationFileManager> manager = AnnotationFileManager.getAnnotationFileManager(module);
                LOG.info("Would like to build: " + psiFile.getName());
                assert manager.isPresent();
                manager.get().requestRebuild(psiFile);
            }
        }

    }

    public static boolean isPsiFileInProject(Project project, PsiFileSystemItem file) {
        return ProjectRootManager.getInstance(project).getFileIndex().isInContent(file.getVirtualFile());
    }
}
