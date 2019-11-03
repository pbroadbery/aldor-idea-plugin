package aldor.annotations;

import aldor.build.module.AldorModuleType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class SaveActionProcessor implements FileDocumentManagerListener {
    private static final Logger LOG = Logger.getInstance(SaveActionProcessor.class);
    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        Collection<PsiFile> psiFiles = new ArrayList<>();
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
            if ((psiFile != null) && isPsiFileInProject(project, psiFile)) {
                psiFiles.add(psiFile);
            }
        }

        for (PsiFile psiFile: psiFiles) {
            Project project = psiFile.getProject();
            Module module = ProjectFileIndex.getInstance(project).getModuleForFile(psiFile.getVirtualFile());
            if ((module == null) || !ModuleType.get(module).equals(AldorModuleType.instance())) {
                continue;
            }
            AnnotationFileManager manager = AnnotationFileManager.getAnnotationFileManager(project);
            LOG.info("Would like to build: " + psiFile.getName());
            manager.requestRebuild(psiFile);
        }
    }


    public static boolean isPsiFileInProject(Project project, PsiFileSystemItem file) {
        return ProjectRootManager.getInstance(project).getFileIndex().isInContent(file.getVirtualFile());
    }


}
