package aldor.build.module;

import com.intellij.openapi.vfs.VirtualFile;

/** Maybe make this an interface */
public class AnnotationFileBuilder {

    public void invokeRebuild(VirtualFile virtualFile) {
    }

    /*

    private static final class CompilerResult {
        private final boolean aborted;
        private final int errors;
        private final int warnings;


        CompilerResult(boolean aborted, int errors, int warnings) {
            this.aborted = aborted;
            this.errors = errors;
            this.warnings = warnings;
        }
    }


    private void invokeBuild(PsiFile psiFile) {
        final VirtualFile file = psiFile.getVirtualFile();
        final VirtualFileSystem vfs = file.getFileSystem();
        final BlockingQueue<AnnotationFileManager.CompilerResult> resultLater = new ArrayBlockingQueue<>(1);

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CompilerManager compilerManager = CompilerManager.getInstance(collectedInfo.project());
                compilerManager.compile(new VirtualFile[]{collectedInfo.file()}, new CompileStatusNotification() {
                    @Override
                    public void finished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
                        resultLater.add(new TypeAndRefAnnotator.CompilerResult(aborted, errors, warnings));
                    }
                });
            }
        });


        try {
            String buildPath = collectedInfo.buildPath();
            LOG.info("Reading file: " + buildPath);
            assert buildPath != null;
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
    }
    */
}
