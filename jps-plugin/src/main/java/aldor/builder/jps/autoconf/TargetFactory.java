package aldor.builder.jps.autoconf;

import aldor.builder.jps.autoconf.descriptors.AbstractTargetDescriptor;
import aldor.builder.jps.autoconf.descriptors.InstantiableBuildTargetType;
import aldor.builder.jps.autoconf.descriptors.PhonyTargetDescriptor;
import aldor.builder.jps.autoconf.descriptors.ScriptTargetDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetRegistry;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.model.module.JpsModule;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TargetFactory {
    private final Map<Class<? extends AbstractTargetDescriptor>, InstantiableBuildTargetType<?,?,?>> typeForDescriptorMap = new HashMap<>();

    public TargetFactory() {
    }

    // FIXME: Circular ref order means we can't prepopulate
    Map<Class<? extends AbstractTargetDescriptor>, InstantiableBuildTargetType<?, ?, ?>> typeForDescriptorMap() {
        final Map<Class<? extends AbstractTargetDescriptor>, InstantiableBuildTargetType<?,?,?>> typeForDescriptorMap = new HashMap<>();
        typeForDescriptorMap.put(PhonyTargetDescriptor.class, PhonyTargets.ident.findType());
        typeForDescriptorMap.put(ScriptTargetDescriptor.class, ScriptBuildTargetType.ident.findType());
        return typeForDescriptorMap;
    }

    @SuppressWarnings("unchecked") @Nullable
    <R, D, T extends BuildTarget<?>> T createTarget(JpsModule module, AbstractTargetDescriptor descriptor) {
        InstantiableBuildTargetType<?, ?, ?> instantiator = typeForDescriptorMap().get(descriptor.getClass());
        if (instantiator == null) {
            return null;
        }
        D myDescriptor = (D) descriptor;
        InstantiableBuildTargetType<?, D, T> myInstantiator = (InstantiableBuildTargetType<?, D, T>) instantiator;
        return myInstantiator.newTarget(module, myDescriptor);
    }


    public static Set<BuildTarget<?>> transitiveDependencies(BuildTargetRegistry idx, List<? extends BuildTarget<?>> targetsIn) {
        Set<BuildTarget<?>> current = new HashSet<>(targetsIn);
        Set<BuildTarget<?>> targets = new HashSet<>(targetsIn);
        TargetOutputIndex dummyIndex = new TargetOutputIndex() {
            @Override
            public Collection<BuildTarget<?>> getTargetsByOutputFile(@NotNull File file) {
                return Collections.emptyList();
            }
        };

        while (!current.isEmpty()) {
            Set<BuildTarget<?>> next = new HashSet<>();
            for (BuildTarget<?> target : current) {
                for (BuildTarget<?> depTarget : target.computeDependencies(idx, dummyIndex)) {
                    if (!targets.contains(depTarget)) {
                        next.add(depTarget);
                    }
                }
            }
            targets.addAll(next);
            current = next;
        }
        return targets;
    }

}
