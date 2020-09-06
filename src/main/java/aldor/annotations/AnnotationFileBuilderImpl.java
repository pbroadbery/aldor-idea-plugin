package aldor.annotations;

import com.google.common.util.concurrent.SettableFuture;
import com.intellij.compiler.impl.OneProjectItemCompileScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
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
                CompileScope scope = new OneProjectItemCompileScope(psiFile.getProject(), psiFile.getVirtualFile().getParent());
                scope.putUserData(Key.<Boolean>create("ao-only"), true);
                CompilerManager compilerManager = CompilerManager.getInstance(project);
                compilerManager.compile(scope, new CompileStatusNotification() {
                    @Override
                    public void finished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
                        LOG.info("Rebuilt " + psiFile.getContainingFile().getName() + ": " + errors + " errors, " + warnings + " warnings. aborted: " + aborted);
                        for (CompilerMessage message : compileContext.getMessages(CompilerMessageCategory.INFORMATION)) {
                            LOG.info("INFORMATION: " + message);
                        }
                        for (CompilerMessage message : compileContext.getMessages(CompilerMessageCategory.ERROR)) {
                            LOG.info("ERROR: " + message);
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
