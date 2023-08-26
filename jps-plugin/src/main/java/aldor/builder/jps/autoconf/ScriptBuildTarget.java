package aldor.builder.jps.autoconf;

import aldor.builder.jps.autoconf.descriptors.AbstractTargetDescriptor;
import aldor.builder.jps.autoconf.descriptors.BuildInstanceModel;
import aldor.builder.jps.autoconf.descriptors.ScriptTargetDescriptor;
import aldor.builder.jps.autoconf.descriptors.ScriptType;
import aldor.builder.jps.util.Sx;
import aldor.util.SxForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetRegistry;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.module.JpsModule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;
import static aldor.util.SxFormUtils.tagged;

class ScriptBuildTarget extends Sx.BuildTarget<SimpleSourceRoot> {
    private final Set<File> outputRoots;
    private final ScriptTargetDescriptor descriptor;
    private final JpsModule module;
    @SuppressWarnings("FieldHasSetterButNoGetter")
    private SimpleSourceRoot rootDescriptor = null;
    private final ScriptBuildTargetType scriptTT;

    ScriptBuildTarget(ScriptBuildTargetType tt, JpsModule module, ScriptTargetDescriptor descriptor) {
        super(tt);
        this.scriptTT = tt;
        this.module = module;
        this.descriptor = descriptor;
        this.outputRoots = new HashSet<>();
    }

    @Override
    @NotNull
    public SxForm sxForm() {
        return list()
                .add(name("ScriptBuildTarget"))
                .add(tagged()
                        .with("id", name(getId()))
                        .with("scriptTargetDescriptor", descriptor.sxForm()));
    }


    @Override
    public String getId() {
        return scriptTT.encode(module, descriptor);
    }

    @Override
    public boolean equals(Object obj) {
        // Not ideal; should be equality on components of id.
        return (this.getClass() == obj.getClass())
                && this.getId().equals( ((ScriptBuildTarget) obj).getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
        BuildInstanceModel model = BuildStaticState.instance().instanceModels().stream().filter(mdl -> mdl.jpsModule().getName().equals(module.getName())).findAny().orElse(null);
        var descriptors = model.dependencies(descriptor);
        List<BuildTarget<?>> list = new ArrayList<>();
        for (AbstractTargetDescriptor desc : descriptors) {
            BuildTarget<?> targetFactoryTarget = scriptTT.targetFactory().createTarget(module, desc);
            list.add(targetFactoryTarget);
        }
        return list;
    }
    @Override
    public @NotNull List<SimpleSourceRoot> computeRootDescriptors(JpsModel model, ModuleExcludeIndex index,
                                                                  IgnoredFileIndex ignoredFileIndex, BuildDataPaths dataPaths) {
        Collection<ScriptTargetDescriptor.RootDescriptor> roots = descriptor.rootDescriptors();
        AtomicInteger counter = new AtomicInteger(0);
        return roots.stream().map(desc -> new SimpleSourceRoot(this, Integer.toString(counter.incrementAndGet()), desc.pattern())).toList();
    }

    @Override
    public SimpleSourceRoot findRootDescriptor(String rootId,
                                                     BuildRootIndex rootIndex) {
        return rootIndex.getTargetRoots(this, null).get(Integer.parseInt(rootId));
    }

    @Override
    public @NotNull String getPresentableName() {
        return switch (descriptor.scriptType().kind()) {
            case Make ->
                    descriptor.scriptType().kind().name() + " " + descriptor.scriptType().targetName() + " in " + descriptor.subdirectory();
            default -> descriptor.scriptType().kind().name();
        };
    }

    @Override
    public @NotNull Collection<File> getOutputRoots(CompileContext context) {
        return descriptor.outputRoots();
    }

    public ScriptType scriptType() {
        return descriptor.scriptType();
    }

    public void addOutputRoot(File root) {
        outputRoots.add(root);
    }

    public ScriptTargetDescriptor scriptDescriptor() {
        return descriptor;
    }
}
