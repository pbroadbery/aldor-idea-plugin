package aldor.psi.elements;

import aldor.language.AldorLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;

public class AldorElementType extends IElementType {
  public AldorElementType(@NonNls String debugName) {
     super(debugName, AldorLanguage.INSTANCE);
  }

  @Override
  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return "Properties:" + super.toString();
  }
}
