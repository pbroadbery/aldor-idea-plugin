package aldor.annotations;

import aldor.build.builders.AldorBuildTargetScopeProvider;
import aldor.build.module.AldorModuleFacade;
import com.intellij.compiler.impl.CompileScopeUtil;
import com.intellij.compiler.impl.FileSetCompileScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AnnotationFileBuilderImpl implements AnnotationFileBuilder {
    private static final Logger LOG = Logger.getInstance(AnnotationFileBuilderImpl.class);

    @Override
    public CompletableFuture<Void> invokeAnnotationBuild(PsiFile psiFile) {
        final Project project = psiFile.getProject();
        Module module = ProjectFileIndex.getInstance(psiFile.getProject()).getModuleForFile(psiFile.getVirtualFile());
        if (!AldorModuleFacade.isAldorModule(module)) {
            return CompletableFuture.completedFuture(null);
        }
        final CompletableFuture<Void> completion = new CompletableFuture();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CompileScope scope = new FileSetCompileScope(Set.of(psiFile.getVirtualFile()), Module.EMPTY_ARRAY);
                scope = new FileSetCompileScope(Set.of(), Module.EMPTY_ARRAY);
                CompileScopeUtil.setBaseScopeForExternalBuild(scope, AldorBuildTargetScopeProvider.scopeForOneFile(project, psiFile.getVirtualFile()));
                scope.putUserData(Key.<Boolean>create("ao-only"), true);
                CompilerManager compilerManager = CompilerManager.getInstance(project);
                compilerManager.compile(scope, new CompileStatusNotification() {
                    @Override
                    public void finished(boolean aborted, int errors, int warnings, @NotNull CompileContext compileContext) {
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
                        completion.complete(null);
                    }
                });
            }
        });
        return completion;
    }

}
