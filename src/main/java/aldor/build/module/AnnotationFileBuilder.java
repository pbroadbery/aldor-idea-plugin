package aldor.build.module;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiFile;

/** Maybe make this an interface */
public interface AnnotationFileBuilder {

    void invokeAnnotationBuild(PsiFile psiFile);

    final class CompilerResult {
        private final boolean aborted;
        private final int errors;
        private final int warnings;


        CompilerResult(boolean aborted, int errors, int warnings) {
            this.aborted = aborted;
            this.errors = errors;
            this.warnings = warnings;
        }
    }

    class AnnotationFileBuilderImpl implements AnnotationFileBuilder {
        private static final Logger LOG = Logger.getInstance(AnnotationFileBuilderImpl.class);

        @Override
        public void invokeAnnotationBuild(PsiFile psiFile) {

            final VirtualFile file = psiFile.getVirtualFile();
            final VirtualFileSystem vfs = file.getFileSystem();
            final Project project = psiFile.getProject();
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    CompilerManager compilerManager = CompilerManager.getInstance(project);
                    compilerManager.compile(new VirtualFile[]{psiFile.getVirtualFile()}, new CompileStatusNotification() {
                        @Override
                        public void finished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
                            Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(file);
                            if (module != null) {
                                LOG.info("Rebuilt " + psiFile.getContainingFile().getName() + ": " + errors + " errors, " + warnings + " warnings. aborted: " + aborted);
                                for (CompilerMessage message : compileContext.getMessages(CompilerMessageCategory.ERROR)) {
                                    LOG.info("Message: " + message);
                                }
                                AnnotationFileManager annotationManager = AnnotationFileManager.getAnnotationFileManager(module);
                                assert annotationManager != null;
                                annotationManager.invalidate(file);
                                compileContext.getMessages(CompilerMessageCategory.ERROR);
                            }
                        }
                    });
                }
            });
        }


        /*
        try {
            String buildPath = collectedInfo.buildPath();
            LOG.info("Reading file: " + buildPath);
            assert buildPath !=  null;
            TypeAndRefAnnotator.CompilerResult result = resultLater.poll(COMPILE_TIMEOUT, TimeUnit.SECONDS);
            if (result == null) {
                return new TypeAndRefAnnotator.FullInfo(collectedInfo, "Time out while compiling " + file.getPresentableName());
            }

            VirtualFile buildFile = vfs.findFileByPath(buildPath);
            if (buildFile == null) {
                return new TypeAndRefAnnotator.FullInfo(collectedInfo, "Compile succeeded, but file apparently doesn't exist (" + buildPath + ")");
            }
            else if (!buildFile.exists()) {
                return new TypeAndRefAnnotator.FullInfo(collectedInfo, "Compile succeeded, but file " + buildFile + " not created");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(buildFile.getInputStream(), StandardCharsets.US_ASCII));
            PopulatedAnnotationFile annotation = new PopulatedAnnotationFile(file.getPath(), SExpression.read(reader));
            return new TypeAndRefAnnotator.FullInfo(collectedInfo, annotation);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return new TypeAndRefAnnotator.FullInfo(collectedInfo, "Interrupt while waiting for the build");
        } catch (IOException e) {
            return new TypeAndRefAnnotator.FullInfo(collectedInfo, "Failed to read annotation file: " + e.getMessage());
        }
        catch (SExpressionReadException e) {
            return new TypeAndRefAnnotator.FullInfo(collectedInfo, "Failed to parse annotation file: "+ e.getMessage());
        }
        */
    }

}
