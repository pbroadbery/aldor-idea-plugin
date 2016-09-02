package aldor.list;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public final class ListFileType extends LanguageFileType {
  @SuppressWarnings("TypeMayBeWeakened")
  public static final ListFileType INSTANCE = new ListFileType();

  private ListFileType() {
    super(ListLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "List file";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "List language file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "list";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AllIcons.FileTypes.Custom;
  }
}