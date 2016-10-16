package aldor.build.module;

import aldor.symbolfile.AnnotationFile;
import aldor.symbolfile.SrcPos;
import aldor.symbolfile.Syme;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public class MissingAnnotationFile implements AnnotationFile {
    private final VirtualFile file;
    private final String errorMessage;

    public MissingAnnotationFile(VirtualFile virtualFile, String errorMessage) {
        this.file = virtualFile;
        this.errorMessage = errorMessage;
    }

    @Override
    public String sourceFile() {
        return file.getPath();
    }

    @Override
    public String errorMessage() {
        return errorMessage;
    }

    @Nullable
    @Override
    public Syme lookupSyme(SrcPos srcPos) {
        return null;
    }
}
