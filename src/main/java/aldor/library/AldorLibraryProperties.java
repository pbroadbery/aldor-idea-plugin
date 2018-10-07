package aldor.library;

import com.intellij.openapi.roots.libraries.LibraryProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AldorLibraryProperties extends LibraryProperties<AldorLibraryProperties> {
    @SuppressWarnings("FieldCanBeLocal")
    private final AldorLibraryDescriptor descriptor;

    public AldorLibraryProperties(AldorLibraryDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Nullable
    @Override
    public AldorLibraryProperties getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull AldorLibraryProperties state) {

    }

    public static final class AldorLibraryDescriptor {
        @Nullable
        private final String sourceDirectory;

        public AldorLibraryDescriptor() {
            this.sourceDirectory = null;
        }

    }

}
