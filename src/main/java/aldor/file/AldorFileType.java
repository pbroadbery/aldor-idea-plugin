package aldor.file;

import aldor.language.AldorLanguage;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;


public final class AldorFileType extends LanguageFileType {
  @SuppressWarnings("TypeMayBeWeakened")
  public static final AldorFileType INSTANCE = new AldorFileType();

  private AldorFileType() {
    super(AldorLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Aldor";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Aldor language file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "as";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AllIcons.FileTypes.Custom;
  }
}
