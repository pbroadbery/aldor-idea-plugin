package aldor.builder.jps;

import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

/**
 * @author nik
 */
public class AldorModuleExtensionRole extends JpsElementChildRoleBase<JpsSimpleElement<JpsAldorModuleProperties>>  {
  public static final AldorModuleExtensionRole INSTANCE = new AldorModuleExtensionRole();

  protected AldorModuleExtensionRole() {
    super("aldor-module-properties");
  }
}
