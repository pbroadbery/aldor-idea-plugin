package aldor.symbolfile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface AnnotationFile {

    @Nullable
    Syme symeForNameAndCode(String name, int typeCode);

    @Nullable
    String sourceFile();

    @Nullable
    String errorMessage();

    @NotNull
    Collection<Syme> lookupSyme(SrcPos srcPos);
}
