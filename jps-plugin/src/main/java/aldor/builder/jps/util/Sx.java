package aldor.builder.jps.util;

import aldor.util.HasSxForm;

import java.util.Collection;

@SuppressWarnings("ClassNameSameAsAncestorName")
public class Sx {
    public abstract static class BuildTargetType<T extends org.jetbrains.jps.builders.BuildTarget<?>>
            extends org.jetbrains.jps.builders.BuildTargetType<T>
            implements HasSxForm {

        protected BuildTargetType(String typeId) {
            super(typeId);
        }

        protected BuildTargetType(String typeId, boolean fileBased) {
            super(typeId, fileBased);
        }
    }

    public abstract static class BuildRootDescriptor extends org.jetbrains.jps.builders.BuildRootDescriptor implements HasSxForm {

    }

    public abstract static class BuildTarget<R extends BuildRootDescriptor> extends org.jetbrains.jps.builders.BuildTarget<R> implements HasSxForm {
        protected BuildTarget(BuildTargetType<? extends BuildTarget<R>> targetType) {
            super(targetType);
        }
    }

    public abstract static class TargetBuilder<R extends BuildRootDescriptor, T extends BuildTarget<R>>
            extends org.jetbrains.jps.incremental.TargetBuilder<R, T>
            implements HasSxForm {
        protected TargetBuilder(Collection<? extends BuildTargetType<? extends T>> buildTargetTypes) {
            super(buildTargetTypes);
        }
    }

    public interface FileFilter extends java.io.FileFilter, HasSxForm {

    }

}
