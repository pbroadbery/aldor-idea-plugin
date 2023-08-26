package aldor.builder.jps.autoconf.descriptors;

import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.model.module.JpsModule;

public interface InstantiableBuildTargetType<R extends BuildRootDescriptor, D, T extends BuildTarget<R>> {
        T newTarget(JpsModule module, D descriptor);
    }
