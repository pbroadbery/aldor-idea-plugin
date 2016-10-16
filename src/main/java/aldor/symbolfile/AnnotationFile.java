package aldor.symbolfile;

import org.jetbrains.annotations.Nullable;

public interface AnnotationFile {

    String sourceFile();

    @Nullable
    String errorMessage();

    @Nullable
    Syme lookupSyme(SrcPos srcPos);
}