package aldor.build.facet.fricas;

import aldor.builder.jps.SpadFacetProperties;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FricasFacetProperties implements SpadFacetProperties {
    @Nullable
    @Attribute
    private String sdkName;

    public FricasFacetProperties() {
        this.sdkName = null;
    }

    public FricasFacetProperties(@Nullable String name) {
        this.sdkName = name;
    }

    @Override
    public String sdkName() {
        return sdkName;
    }

    public void setSdkName(@Nullable String sdkName) {
        this.sdkName = sdkName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        FricasFacetProperties other = (FricasFacetProperties) o;
        return Objects.equals(sdkName, other.sdkName());
    }

    @Override
    public int hashCode() {
        //noinspection ObjectInstantiationInEqualsHashCode
        return Objects.hash(sdkName);
    }
}
