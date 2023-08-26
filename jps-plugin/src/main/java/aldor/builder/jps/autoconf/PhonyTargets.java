package aldor.builder.jps.autoconf;

import aldor.builder.AldorBuildConstants;
import aldor.builder.jps.autoconf.descriptors.AbstractTargetDescriptor;
import aldor.builder.jps.autoconf.descriptors.BuildInstanceModel;
import aldor.builder.jps.autoconf.descriptors.InstantiableBuildTargetType;
import aldor.builder.jps.autoconf.descriptors.PhonyTargetDescriptor;
import aldor.builder.jps.util.Sx;
import aldor.util.SxForm;
import aldor.util.TargetTypeIdentifier;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetLoader;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static aldor.util.SxFormUtils.list;
import static aldor.util.SxFormUtils.name;

public final class PhonyTargets {
    public static final TargetTypeIdentifier<PhonyTargetType> ident = new TargetTypeIdentifier<>(PhonyTargetType.class, PhonyTargetType.ID);

    // Phony in  the 'make' sense - having no build rule, but with dependencies
    public static class PhonyTargetType
            extends Sx.BuildTargetType<PhonyTarget>
            implements InstantiableBuildTargetType<EmptyRootDescriptor, PhonyTargetDescriptor, PhonyTarget> {
        public static final String ID = AldorBuildConstants.PHONY_ALDOR_FILE_TARGET;
        private static final Logger LOG = Logger.getInstance(PhonyTargetType.class);
        private final BuildStaticState staticState;
        private final TargetFactory targetFactory;

        public PhonyTargetType(BuildStaticState staticState, TargetFactory targetFactory) {
            super(ID, true);
            this.staticState = staticState;
            this.targetFactory = targetFactory;
        }

        @Override
        public @NotNull SxForm sxForm() {
            return list().add(name(ID));
        }

        @Override
        public @NotNull List<PhonyTarget> computeAllTargets(@NotNull JpsModel jpsModel) {
            Collection<BuildInstanceModel> instanceModels = reloadInstanceModels(jpsModel);
            LOG.info("Compute all: Phony " + jpsModel.getProject().getName() + " models: " + instanceModels.size());

            var targets = instanceModels.stream()
                    .peek(model -> LOG.info("Targets for " + model.sxForm().asSExpression()))
                    .flatMap(instanceModel -> instanceModel.allPhonyTargets().stream().map(x -> Pair.of(instanceModel, x)))
                    .map(pair -> new PhonyTarget(this, pair.getLeft().jpsModule(), pair.getRight()))
                    .toList();
            LOG.info("Found " + targets.size() + " targets");
            return targets;
        }

        @NotNull
        private Collection<BuildInstanceModel> reloadInstanceModels(@NotNull JpsModel jpsModel) {
            Collection<BuildInstanceModel> instanceModels = staticState.updateJpsModel(jpsModel);
            if (instanceModels == null) {
                throw new IllegalStateException("Models should be set");
            }

            return instanceModels;
        }

        @Override
        public @NotNull BuildTargetLoader<PhonyTarget> createLoader(@NotNull JpsModel jpsModel) {
            LOG.info("Create loader..");
            var mapping = computeAllTargets(jpsModel).stream()
                    .collect(Collectors.toMap(tgt -> tgt.getId(), tgt -> tgt));
            return new BuildTargetLoader<>() {
                @Override
                @Nullable
                public PhonyTarget createTarget(@NotNull String targetId) {
                    return mapping.get(targetId);
                }
            };
        }

        public PhonyTarget newTarget(JpsModule module, PhonyTargetDescriptor descriptor) {
            return new PhonyTarget(this, module, descriptor);
        }

        public TargetFactory targetFactory() {
            return targetFactory;
        }
    }

    public static class PhonyTarget extends Sx.BuildTarget<EmptyRootDescriptor> {
        private final PhonyTargetDescriptor descriptor;
        private final JpsModule module;
        private final PhonyTargetType phonyTT;

        protected PhonyTarget(PhonyTargetType targetType, JpsModule module, PhonyTargetDescriptor descriptor) {
            super(targetType);
            this.phonyTT = targetType;
            this.module = module;
            this.descriptor = descriptor;
        }

        @Override
        public @NotNull SxForm sxForm() {
            return list().add(name("DummyTarget")).add(descriptor.sxForm());
        }

        @Override
        public String getId() {
            return descriptor.id();
        }

        @Override
        public boolean equals(Object obj) {
            return (this.getClass() == obj.getClass()) && this.getId().equals(((PhonyTarget) obj).getId());
        }

        @Override
        public int hashCode() {
            return getId().hashCode();
        }

        @Override
        public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
            BuildInstanceModel model = BuildStaticState.instance().instanceModels().stream().filter(mdl -> mdl.jpsModule().getName().equals(module.getName())).findAny().orElse(null);
            var descriptors = model.dependencies(this.descriptor);
            List<BuildTarget<?>> list = new ArrayList<>();
            for (AbstractTargetDescriptor desc : descriptors) {
                BuildTarget<?> targetFactoryTarget = phonyTT.targetFactory().createTarget(module, desc);
                list.add(targetFactoryTarget);
            }
            return list;
        }

        @Override @NotNull
        public List<EmptyRootDescriptor> computeRootDescriptors(JpsModel model, ModuleExcludeIndex index, IgnoredFileIndex ignoredFileIndex, BuildDataPaths dataPaths) {
            return Collections.emptyList();
        }

        @Override @Nullable
        public EmptyRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
            return null;
        }

        @Override
        public @NotNull String getPresentableName() {
            return descriptor.presentableName();
        }

        @Override
        public @NotNull Collection<File> getOutputRoots(CompileContext context) {
            return Collections.emptyList();
        }

        public PhonyTargetDescriptor descriptor() {
            return descriptor;
        }
    }

    private static class EmptyRootDescriptor extends Sx.BuildRootDescriptor {

        private final BuildTarget<?> target;

        EmptyRootDescriptor(BuildTarget<?> target) {
            this.target = target;
        }

        @Override
        public @NotNull SxForm sxForm() {
            return list().add(name("EmptyRootDescriptor"));
        }

        @Override
        public String getRootId() {
            return "empty";
        }

        @Override
        public File getRootFile() {
            return new File(".");
        }

        @Override
        public BuildTarget<?> getTarget() {
            return target;
        }
    }

}
