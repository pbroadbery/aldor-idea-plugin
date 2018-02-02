package aldor.file;

import aldor.language.SpadLanguage;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class SpadInputFileType  extends LanguageFileType {
    @SuppressWarnings("TypeMayBeWeakened")
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
        return IconUtil.addText(AllIcons.FileTypes.Custom, "In");
    }

}
