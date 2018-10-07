package aldor.build.module;

import com.google.common.util.concurrent.SettableFuture;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.util.concurrent.Future;

public class AnnotationFileBuilderImpl implements AnnotationFileBuilder {
    private static final Logger LOG = Logger.getInstance(AnnotationFileBuilderImpl.class);

    @Override
    public Future<Void> invokeAnnotationBuild(PsiFile psiFile) {
        final Project project = psiFile.getProject();
        final SettableFuture<Void> completion = SettableFuture.create();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CompilerManager compilerManager = CompilerManager.getInstance(project);
                compilerManager.compile(new VirtualFile[]{psiFile.getVirtualFile()}, new CompileStatusNotification() {
                    @Override
                    public void finished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
                        LOG.info("Rebuilt " + psiFile.getContainingFile().getName() + ": " + errors + " errors, " + warnings + " warnings. aborted: " + aborted);
                        for (CompilerMessage message : compileContext.getMessages(CompilerMessageCategory.ERROR)) {
                            LOG.info("Message: " + message);
                        }
                        AnnotationFileManager annotationManager = AnnotationFileManager.getAnnotationFileManager(project);
                        annotationManager.invalidate(psiFile);
                        compileContext.getMessages(CompilerMessageCategory.ERROR);
                        completion.set(null);
                    }
                });
            }
        });
        return completion;
    }

}
