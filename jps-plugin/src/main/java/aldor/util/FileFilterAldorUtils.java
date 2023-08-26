package aldor.util;

import aldor.builder.jps.util.Sx;
import com.intellij.openapi.util.io.FileUtilRt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;

public final class FileFilterAldorUtils {

    public static FileFilter or(FileFilter filter1, FileFilter filter2) {
        return file -> filter1.accept(file) || filter2.accept(file);
    }

    public static FileFilter nameFileFilter(@NotNull String name) {
        return file -> file.getName().equals(name);
    }

    public static SxFormFileFilter only(File file) {
        return new SxFormFileFilter(() -> list().add(name("only")).add(SxFormUtils.file(file)),
                new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return FileUtilRt.filesEqual(file, pathname);
                    }

                    // TODO: Should be directory aware
                });
    }

    public static SxFormFileFilter dirWithExtension(String directory, String extension) {
        return new SxFormFileFilter(() -> list().add(name("dir-with-extension")).add(SxFormUtils.stringified(directory)).add(SxFormUtils.stringified(extension)),
                new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getParent().equals(directory) && FileUtilRt.extensionEquals(pathname.getName(), extension);
                    }
                });
    }

    public static SxFormFileFilter withExtension(String extension) {
        return new SxFormFileFilter(() -> list().add(name("with-extension")).add(SxFormUtils.stringified(extension)),
                new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return FileUtilRt.extensionEquals(pathname.getName(), extension);
                    }
                });
    }

    public static SxFormFileFilter logged(Consumer<String> logger, SxFormFileFilter filter) {
        return new SxFormFileFilter(() -> list().add(name("logged")).add(filter.sxForm()),
                name -> {
                    boolean result = filter.accept(name);
                    logger.accept("Consume " + result + " for " + name + " " + filter.sxForm());
                    return result;
                });

    }

    public static SxFormFileFilter all() {
        return new SxFormFileFilter(() -> list().add(name("all")),
                (f) -> true);
    }

    public static class SxFormFileFilter implements Sx.FileFilter {
        private final Supplier<SxForm> sxFormSupplier;
        private final FileFilter fileFilter;

        public SxFormFileFilter(Supplier<SxForm> sxFormSupplier, FileFilter fileFilter) {
            this.sxFormSupplier = sxFormSupplier;
            this.fileFilter = fileFilter;
        }

        @Override
        public SxForm sxForm() {
            return sxFormSupplier.get();
        }

        @Override
        public boolean accept(File pathname) {
            return fileFilter.accept(pathname);
        }

        public SxFormFileFilter or(SxFormFileFilter other) {
            SxFormFileFilter self = this;
            return new SxFormFileFilter(() -> list().add(name("or")).add(sxForm()).add(other.sxForm()),
                    new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return self.accept(pathname) || other.accept(pathname);
                        }
                    });
        }
    }
}
