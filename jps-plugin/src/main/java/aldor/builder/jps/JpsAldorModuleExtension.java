/*
 * Copyright 2012-2014 Sergey Ignatov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aldor.builder.jps;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.module.JpsModule;

  public class JpsAldorModuleExtension extends JpsCompositeElementBase<JpsAldorModuleExtension>  {
  public static final JpsElementChildRole<JpsAldorModuleExtension> ROLE = JpsElementChildRoleBase.create("AldorExtensionProperties"); // FIXME: Use Aldor once working

  private final AldorModuleExtensionProperties myProperties;

  @SuppressWarnings("UnusedDeclaration")
  public JpsAldorModuleExtension() {
    myProperties = new AldorModuleExtensionProperties();
  }

  public JpsAldorModuleExtension(AldorModuleExtensionProperties properties) {
    myProperties = properties;
  }

  private JpsAldorModuleExtension(JpsAldorModuleExtension moduleExtension) {
    myProperties = moduleExtension.myProperties.asBuilder().build();
  }

  @NotNull
  @Override
  public JpsAldorModuleExtension createCopy() {
    return new JpsAldorModuleExtension(this);
  }

  public AldorModuleExtensionProperties getProperties() {
    return myProperties;
  }

  @Nullable
  public static JpsAldorModuleExtension getExtension(@SuppressWarnings("TypeMayBeWeakened") @Nullable JpsModule module) {
    return (module != null) ? module.getContainer().getChild(ROLE) : null;
  }
}
