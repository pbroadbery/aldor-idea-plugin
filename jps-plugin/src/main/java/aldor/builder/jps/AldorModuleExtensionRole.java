package aldor.builder.jps;

import aldor.builder.jps.module.AldorModuleState;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

public class AldorModuleExtensionRole extends JpsElementChildRoleBase<JpsSimpleElement<AldorModuleState>>  {
  public static final AldorModuleExtensionRole INSTANCE = new AldorModuleExtensionRole();

  protected AldorModuleExtensionRole() {
    super("aldor-module-state");
  }
}
