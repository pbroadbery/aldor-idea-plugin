package aldor;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AldorFileType extends LanguageFileType {
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
    return "aldor";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AllIcons.FileTypes.Custom;
  }
}