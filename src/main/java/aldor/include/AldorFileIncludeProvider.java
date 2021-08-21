package aldor.include;

import aldor.file.AldorFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.include.FileIncludeInfo;
import com.intellij.psi.impl.include.FileIncludeProvider;
import com.intellij.util.Consumer;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AldorFileIncludeProvider extends FileIncludeProvider {
    private static final int VERSION = 1;

    @Override
    @NotNull
    public String getId() {
        return "aldor-include";
    }

    @Override
    public boolean acceptFile(@NotNull VirtualFile file) {
        final FileType type = file.getFileType();
        return type == AldorFileType.INSTANCE;
    }

    @Override
    public void registerFileTypesUsedForIndexing(@NotNull Consumer<? super FileType> fileTypeSink) {
        fileTypeSink.consume(AldorFileType.INSTANCE);
    }

    @Override
    public FileIncludeInfo[] getIncludeInfos(@NotNull FileContent content) {
        // Should really look for include tokens, but...
        CharSequence text = content.getContentAsText();
        List<FileIncludeInfo> infos = new ArrayList<>();
        SysCommandSearch.instance().searchIncludes(content.getContentAsText(), seq -> {
            infos.add(new FileIncludeInfo(seq.toString()));
        });
        return infos.toArray(FileIncludeInfo.EMPTY);
    }

    @Override
    public int getVersion() {
        return VERSION;
    }
}
