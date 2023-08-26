package aldor.builder.jps;

import aldor.util.InstanceCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.ex.JpsElementBase;

import java.util.Objects;

public class AldorSourceRootProperties extends JpsElementBase<AldorSourceRootProperties> {
    private final int id = InstanceCounter.instance().next(AldorSourceRootProperties.class);
    @Nullable
    private String outputDirectory;

    public AldorSourceRootProperties(@Nullable String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public @NotNull AldorSourceRootProperties createCopy() {
        return new AldorSourceRootProperties(outputDirectory);
    }

    @Override
    public void applyChanges(@NotNull AldorSourceRootProperties modified) {
        this.outputDirectory = modified.outputDirectory();
    }

    public void outputDirectory(String outputDirectory) {
        if (!Objects.equals(this.outputDirectory, outputDirectory)) {
            this.outputDirectory = outputDirectory;
            fireElementChanged();
        }
    }

    public String outputDirectory() {
        return outputDirectory;
    }

    @Override
    public String toString() {
        return "AldorSourceRootProperties{" +
                ", id=" + id + ", " +
                "outputDirectory='" + outputDirectory + '\'' +
                '}';
    }
}
