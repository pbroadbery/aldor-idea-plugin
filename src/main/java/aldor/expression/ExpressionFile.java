package aldor.expression;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class ExpressionFile extends PsiFileBase {

    public ExpressionFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, ExpressionLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ExpressionFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Expression File";
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }


}
