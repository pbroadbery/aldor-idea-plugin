package aldor.build.module;

import aldor.symbolfile.AnnotationFile;
import aldor.symbolfile.MissingAnnotationFile;
import aldor.symbolfile.PopulatedAnnotationFile;
import aldor.symbolfile.Syme;
import aldor.util.sexpr.SExpressions;
import aldor.util.sexpr.SymbolPolicy;
import aldor.util.sexpr.impl.SExpressionReadException;
import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import static aldor.builder.files.AldorFileBuildTarget.trimExtension;

public class AnnotationFileManager implements Disposable {
    private static final Logger LOG = Logger.getInstance(AnnotationFileManager.class);

    private static final Key<AnnotationFileManager> MGR_KEY = new Key<>(AnnotationFileManager.class.getSimpleName());
    private final Set<String> dirtyFiles;
    @SuppressWarnings("unused")
    private final Set<String> updatingFiles;
    private final Map<String, AnnotationFile> annotationFileForFile;
    private final Map<String, LineNumberMap> lineNumberMapForFile;
    private final AnnotationFileBuilder annotationFileBuilder;

    @Nullable
    private MessageBusConnection myBusConnection = null;

    public AnnotationFileManager(Project project) {
        dirtyFiles = new HashSet<>();
        updatingFiles = new HashSet<>();
        annotationFileForFile = Maps.newHashMap();
        lineNumberMapForFile = Maps.newHashMap();
        annotationFileBuilder = new AnnotationFileBuilderImpl();
        setupFileWatcher();
    }

    private void setupFileWatcher() {
        myBusConnection = ApplicationManager.getApplication().getMessageBus().connect();
        myBusConnection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull final List<? extends VFileEvent> events) {
                for (VFileEvent event : events) {
                    if ((event instanceof VFileContentChangeEvent) && (event.getFile() != null)) {
                        if (event.getFile().getName().endsWith(".abn")) {
                            invalidate(event.getFile());
                        }
                    }
                }
            }

        });
    }

    @NotNull
    public static AnnotationFileManager getAnnotationFileManager(Project project) {
        AnnotationFileManager mgr = project.getUserData(MGR_KEY);
        if (mgr == null) {
            mgr = new AnnotationFileManager(project);
            project.putUserData(MGR_KEY, mgr);
            Disposer.register(project, mgr);
        }
        return mgr;
    }

    @Override
    public void dispose() {
        LOG.info("Disposing of annotation file manager");
        if (myBusConnection != null) {
            myBusConnection.dispose();
        }
        myBusConnection = null;
    }

    @NotNull
    public AnnotationFile annotationFile(@NotNull PsiFile psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        AldorModuleManager moduleManager = AldorModuleManager.getInstance(psiFile.getProject());
        String buildFilePath = moduleManager.annotationFileForSourceFile(psiFile);

        if (virtualFile == null) {
            return new MissingAnnotationFile(null, "no file");
        }
        if (!annotationFileForFile.containsKey(buildFilePath)) {
            LOG.info("loading annotated file: " + virtualFile);

            AnnotationFile annotationFile = annotatedFile(psiFile);
            annotationFileForFile.put(buildFilePath, annotationFile);
            LineNumberMap map = new LineNumberMap(psiFile);
            this.lineNumberMapForFile.put(buildFilePath, map);

        }
        return annotationFileForFile.get(buildFilePath);
    }

    // ToDo: There's probably a fair amount of missing thread-safety here.
    public void invalidate(PsiFile psiFile) {
        LOG.info("Invalidating " + psiFile);
        AldorModuleManager moduleManager = AldorModuleManager.getInstance(psiFile.getProject());
        String buildFilePath = moduleManager.annotationFileForSourceFile(psiFile);
        annotationFileForFile.remove(buildFilePath);
    }

    public void invalidate(VirtualFile file) {
        assert file.getName().endsWith(".abn");
        annotationFileForFile.remove(file.getPath());
    }

    @NotNull
    private AnnotationFile annotatedFile(PsiFileSystemItem psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        VirtualFileSystem vfs = virtualFile.getFileSystem();

        AldorModuleManager moduleManager = AldorModuleManager.getInstance(psiFile.getProject());
        String buildFilePath = moduleManager.annotationFileForSourceFile(psiFile);
        if (buildFilePath == null) {
            return new MissingAnnotationFile(virtualFile, "No content directory");
        }
        else {
            VirtualFile buildFile = vfs.findFileByPath(buildFilePath);
            /* Could call
             *    buildFile = vfs.refreshAndFindFileByPath(buildFilePath);
             * here, but might deadlock from a non EDT thread, so bottle it.
             */

            LOG.info("Looking for build file: " + buildFilePath);
            if (buildFile == null) {
                return new MissingAnnotationFile(virtualFile, "Missing .abn file: "+ buildFilePath);
            }
            try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(buildFile.getInputStream(), StandardCharsets.US_ASCII))) {

                try {
                    return new PopulatedAnnotationFile(virtualFile.getPath(), SExpressions.read(reader, SymbolPolicy.ALLCAPS));
                }
                catch (SExpressionReadException e) {
                    LOG.error("When reading file: " + buildFilePath + ": " + reader.getLineNumber(), e);
                    return new MissingAnnotationFile(virtualFile, e.getMessage());
                }
            } catch (IOException e) {
                LOG.error("When creating annotation file " + buildFilePath, e);
                return new MissingAnnotationFile(virtualFile, e.getMessage());
            }
            catch (RuntimeException e) {
                LOG.error("Failed to parse annotation file " + buildFilePath, e);
                return new MissingAnnotationFile(virtualFile, e.getMessage());
            }
        }
    }

    @Nullable
    public LineNumberMap lineNumberMapForFile(PsiFile file) {
        AldorModuleManager moduleManager = AldorModuleManager.getInstance(file.getProject());
        String buildFilePath = moduleManager.annotationFileForSourceFile(file);
        if (file.getVirtualFile() == null) {
            return null;
        }
        annotationFile(file);
        return lineNumberMapForFile.get(buildFilePath);
    }


    public Future<Void> requestRebuild(PsiFile psiFile) {
        return annotationFileBuilder.invokeAnnotationBuild(psiFile);
    }

    @Nullable
    public String refSourceFile(Syme syme) {
        Syme original = syme.original();
        @Nullable
        String refName;
        if (original == null) {
            refName = syme.archiveLib();
        }
        else {
            if (original.typeCode() == -1) {
                refName = null;
            }
            else {
                refName = original.library();
            }
        }
        if (refName == null) {
            return null;
        }
        return trimExtension(refName) + ".as";
    }

}


