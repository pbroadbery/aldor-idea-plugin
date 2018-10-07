package aldor.symbolfile;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class MissingAnnotationFile implements AnnotationFile {
    private final VirtualFile file;
    private final String errorMessage;

    public MissingAnnotationFile(VirtualFile virtualFile, String errorMessage) {
        this.file = virtualFile;
        this.errorMessage = errorMessage;
    }

    @Nullable
    @Override
    public Syme symeForNameAndCode(String name, int typeCode) {
        return null;
    }

    @Nullable
    @Override
    public String sourceFile() {
        return (file == null) ? null : file.getPath();
    }

    @Override
    public String errorMessage() {
        return errorMessage;
    }

    @NotNull
    @Override
    public Collection<Syme> lookupSyme(SrcPos srcPos) {
        return Collections.emptyList();
    }
}
