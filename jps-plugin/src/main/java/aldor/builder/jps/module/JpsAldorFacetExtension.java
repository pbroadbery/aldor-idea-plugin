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

package aldor.builder.jps.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleReference;

public class JpsAldorFacetExtension extends JpsCompositeElementBase<JpsAldorFacetExtension> {
  public static final JpsElementChildRole<JpsAldorFacetExtension> ROLE = JpsElementChildRoleBase.create("AldorFacetProperties");

  private final AldorFacetProperties myProperties;

  @SuppressWarnings("UnusedDeclaration")
  public JpsAldorFacetExtension() {
    myProperties = new AldorFacetProperties();
  }

  public JpsAldorFacetExtension(AldorFacetProperties properties) {
    myProperties = properties;
  }

  private JpsAldorFacetExtension(JpsAldorFacetExtension moduleExtension) {
    myProperties = moduleExtension.getProperties().asBuilder().build();
  }

  @NotNull
  @Override
  public JpsAldorFacetExtension createCopy() {
    return new JpsAldorFacetExtension(this);
  }

  public AldorFacetProperties getProperties() {
    return myProperties;
  }

  @Nullable
  public static JpsAldorFacetExtension getExtension(@SuppressWarnings("TypeMayBeWeakened") @Nullable JpsModule module) {
    return (module != null) ? module.getContainer().getChild(ROLE) : null;
  }

  public void install(JpsModule module) {
    module.getContainer().setChild(ROLE, this);
  }


  @Override
  public String toString() {
    return "JpsAldorFacetExtension{" +
            "myProperties=" + myProperties +
            '}';
  }
}
