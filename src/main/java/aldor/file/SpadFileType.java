package aldor.file;

import aldor.language.SpadLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.AldorIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public final class SpadFileType extends LanguageFileType {
    public static final SpadFileType INSTANCE = new SpadFileType();

    private SpadFileType() {
        super(SpadLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Spad";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Spad language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "spad";
    }

    @NotNull @Override
    public Icon getIcon() {
        return AldorIcons.SPAD_FILE;
    }
}