package aldor.builder.files;

import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.model.module.JpsModule;

import java.io.File;

public class AldorFileRootDescriptor extends BuildRootDescriptor {
    private final File path;
    private final BuildTarget<?> target;

    AldorFileRootDescriptor(BuildTarget<?> target, JpsModule module, File file) {
        this.path = file;
        this.target = target;
    }

    @Override
    public String getRootId() {
        return path.getAbsolutePath();
    }

    @Override
    public File getRootFile() {
        return path;
    }

    @Override
    public BuildTarget<?> getTarget() {
        return target;
    }


    public File getFile() {
        return path;
    }
}