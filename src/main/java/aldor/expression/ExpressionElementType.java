package aldor.expression;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;

public class ExpressionElementType extends IElementType {
  public ExpressionElementType(@NonNls String debugName) {
     super(debugName, ExpressionLanguage.INSTANCE);
  }

  @Override
  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return "Properties:" + super.toString();
  }
}