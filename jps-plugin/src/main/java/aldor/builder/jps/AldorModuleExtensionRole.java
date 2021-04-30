package aldor.builder.jps;

import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

public class AldorModuleExtensionRole extends JpsElementChildRoleBase<JpsSimpleElement<AldorModuleExtensionProperties>>  {
  public static final AldorModuleExtensionRole INSTANCE = new AldorModuleExtensionRole();

  protected AldorModuleExtensionRole() {
    super("aldor-module-properties");
  }
}
