package aldor.symbolfile;

import org.jetbrains.annotations.Nullable;

public interface AnnotationFile {

    @Nullable
    Syme symeForNameAndCode(String name, int typeCode);

    String sourceFile();

    @Nullable
    String errorMessage();

    @Nullable
    Syme lookupSyme(SrcPos srcPos);
}
