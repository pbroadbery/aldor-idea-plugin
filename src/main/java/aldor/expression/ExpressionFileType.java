package aldor.expression;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class ExpressionFileType extends LanguageFileType {
    public static final FileType INSTANCE = new ExpressionFileType();
    public static final String FILE_EXTENSION = "expr";

    /**
     * Creates a language file type for the specified language.
     */
    private ExpressionFileType() {
        super(ExpressionLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Expression";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Test language";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return FILE_EXTENSION;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return IconUtil.getMoveDownIcon();
    }
}
