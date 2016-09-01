package aldor.list;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class ListFile extends PsiFileBase {
  public ListFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, ListLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return ListFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "List File";
  }

}