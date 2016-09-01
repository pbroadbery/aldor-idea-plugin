package aldor.list;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;

public class ListElementType extends IElementType {
  public ListElementType(@NonNls String debugName) {
    super(debugName, ListLanguage.INSTANCE);
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return "Properties:" + super.toString();
  }
}