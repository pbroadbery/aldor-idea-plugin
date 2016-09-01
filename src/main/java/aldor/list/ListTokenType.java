package aldor.list;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ListTokenType extends IElementType {
  public ListTokenType(@NotNull @NonNls String debugName) {
    super(debugName, ListLanguage.INSTANCE);
  }

  @Override
  public String toString() {
    return "ListTokenType." + super.toString();
  }
}