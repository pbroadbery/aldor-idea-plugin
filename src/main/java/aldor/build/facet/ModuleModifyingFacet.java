package aldor.build.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerListener;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
@SuppressWarnings({"AbstractClassExtendsConcreteClass", "SerializableHasSerializationMethods"})
public abstract class ModuleModifyingFacet<T extends FacetConfiguration> extends Facet<T> {
  private static final long serialVersionUID = 8039067374436239194L;


  protected ModuleModifyingFacet(@NotNull FacetType facetType,
                                 @NotNull Module module,
                                 @NotNull String name, @NotNull T configuration, Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
    final MessageBusConnection connection = module.getProject().getMessageBus().connect();
    connection.subscribe(FacetManager.FACETS_TOPIC, new ModuleUpdater());
    Disposer.register(this, connection);
  }

  public abstract void updateModule();
  public abstract void facetRemoved();

  private class ModuleUpdater implements FacetManagerListener {
    @Override
    public void beforeFacetRemoved(@NotNull Facet facet) {
      //noinspection ObjectEquality
      if (facet == ModuleModifyingFacet.this) {
        //noinspection unchecked
        ((ModuleModifyingFacet<T>) facet).facetRemoved();
      }
    }

    @Override
    public void facetConfigurationChanged(@NotNull Facet facet) {
      //noinspection ObjectEquality
      if (facet == ModuleModifyingFacet.this) {
        //noinspection unchecked
        ((ModuleModifyingFacet<T>) facet).updateModule();
      }
    }
  };
}
