package aldor.build.facet.aldor;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class ModuleModifyingFacet<T extends FacetConfiguration> extends Facet<T> {

  protected ModuleModifyingFacet(@NotNull FacetType facetType,
                                 @NotNull Module module,
                                 @NotNull String name, @NotNull T configuration, Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
    final MessageBusConnection connection = module.getMessageBus().connect();
    connection.subscribe(FacetManager.FACETS_TOPIC, new FacetManagerAdapter() {
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
    });
    //noinspection ThisEscapedInObjectConstruction
    Disposer.register(this, connection);
  }

  public abstract void updateModule();
  public abstract void facetRemoved();
}
