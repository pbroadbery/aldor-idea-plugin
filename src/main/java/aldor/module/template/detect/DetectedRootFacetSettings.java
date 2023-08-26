package aldor.module.template.detect;

import aldor.builder.jps.module.ConfigRootFacetProperties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DetectedRootFacetSettings {
    private Map<File, ConfigRootFacetProperties> facetPropertiesForRoot = new HashMap<>();

    public DetectedRootFacetSettings() {
    }

    public void put(File dir, ConfigRootFacetProperties facetProperties) {
        facetPropertiesForRoot.put(dir, facetProperties);
    }

    ConfigRootFacetProperties get(File dir) {
        return facetPropertiesForRoot.get(dir);
    }

    public boolean isEmpty() {
        return facetPropertiesForRoot.isEmpty();
    }

    public Map<File, ConfigRootFacetProperties> facetPropertiesForRoot() {
        // TODO: Use proper methods
        return facetPropertiesForRoot;
    }
}
