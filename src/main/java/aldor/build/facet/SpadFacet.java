package aldor.build.facet;

import aldor.builder.jps.SpadFacetProperties;

import java.util.Optional;

public interface SpadFacet<T extends SpadFacetProperties> {
    Optional<T> getProperties();
}
