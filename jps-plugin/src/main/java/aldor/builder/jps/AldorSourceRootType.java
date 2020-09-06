package aldor.builder.jps;

import org.jetbrains.jps.model.JpsDummyElement;
import org.jetbrains.jps.model.ex.JpsElementTypeWithDummyProperties;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

public final class AldorSourceRootType extends JpsElementTypeWithDummyProperties implements JpsModuleSourceRootType<JpsDummyElement> {
  public static final AldorSourceRootType INSTANCE = new AldorSourceRootType(false);
  public static final AldorSourceRootType TEST = new AldorSourceRootType(true);
  private final boolean isTest;

  private AldorSourceRootType(boolean isTest) {
    this.isTest = isTest;
  }

  @Override
  public boolean isForTests() {
    return isTest;
  }
}