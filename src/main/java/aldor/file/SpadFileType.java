package aldor.file;

import aldor.language.SpadLanguage;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Created by pab on 26/09/16.
 */
public final class SpadFileType extends LanguageFileType {
    @SuppressWarnings("TypeMayBeWeakened")
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

    @Nullable
    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Custom;
    }
}