package aldor.builder.jps;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsDummyElement;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.ex.JpsElementTypeWithDummyProperties;
import org.jetbrains.jps.model.module.JpsModuleType;

public class JpsAldorModuleType extends JpsElementTypeWithDummyProperties implements JpsModuleType<JpsDummyElement> {
  public static final JpsAldorModuleType INSTANCE = new JpsAldorModuleType();

  @NotNull
  @Override
  public JpsDummyElement createDefaultProperties() {
    return JpsElementFactory.getInstance().createDummyElement();
  }
}