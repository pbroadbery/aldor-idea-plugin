package aldor.file;

import aldor.language.SpadLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.AldorIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class SpadInputFileType  extends LanguageFileType {
    public static final SpadInputFileType INSTANCE = new SpadInputFileType();

    private SpadInputFileType() {
        super(SpadLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Input";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Spad input file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "input";
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return AldorIcons.SPAD_INPUT_FILE;
    }

}
