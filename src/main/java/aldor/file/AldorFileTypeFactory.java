package aldor.file;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class AldorFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(AldorFileType.INSTANCE);
        fileTypeConsumer.consume(SpadFileType.INSTANCE);

        fileTypeConsumer.consume(new AldorMiscFileType("ALDOROBJ", "Aldor Object", "ao"));
        fileTypeConsumer.consume(new AldorMiscFileType("ALDORFOAM", "Aldor Intermediate", "fm"));
        fileTypeConsumer.consume(new AldorMiscFileType("ALDORLIB", "Aldor Library", "al"));
        fileTypeConsumer.consume(new AldorMiscFileType("ALDOROBJ", "Aldor Object", "ao"));
        fileTypeConsumer.consume(new AldorMiscFileType("ALDORSYM", "Aldor Symbol File", "asy"));
        fileTypeConsumer.consume(new AldorMiscFileType("ALDORLSP", "Aldor Lisp", "lsp"));
        fileTypeConsumer.consume(new AldorMiscFileType("ALDORANN", "Aldor Annotations", "abn"));
    }

    private static final class AldorMiscFileType implements FileType {

        private final String name;
        private final String description;
        private final String extension;

        private AldorMiscFileType(String name, String description, String extension) {
            this.name = name;
            this.description = description;
            this.extension = extension;
        }


        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @NotNull
        @Override
        public String getDescription() {
            return description;
        }

        @NotNull
        @Override
        public String getDefaultExtension() {
            return extension;
        }

        @Nullable
        @Override
        public Icon getIcon() {
            return IconUtil.addText(IconUtil.getMoveDownIcon(), extension);
        }

        @Override
        public boolean isBinary() {
            return true;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Nullable
        @Override
        public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
            return null;
        }
    }

}
