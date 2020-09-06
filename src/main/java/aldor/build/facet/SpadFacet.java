package aldor.build.facet;

import aldor.builder.jps.SpadFacetProperties;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetConfiguration;

import java.util.Optional;

public interface SpadFacet<T extends SpadFacetProperties> {
    Optional<T> getProperties();
}
