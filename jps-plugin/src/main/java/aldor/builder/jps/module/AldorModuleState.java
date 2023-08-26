package aldor.builder.jps.module;

import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

// I'd like this to work, but for now all properties have moved to the facet.
public class AldorModuleState {

    public AldorModuleState() {
    }

    public AldorModuleState(AldorModuleState state) {
        //noinspection StatementWithEmptyBody,VariableNotUsedInsideIf
        if (state != null) {
            //this.makeConvention = state.makeConvention;
        }
    }

    public AldorModuleState(Builder builder) {
    }


    public MakeConvention _makeConvention() {
        throw new UnsupportedOperationException("nope");
    }

    public String _outputDirectory() {
        throw new UnsupportedOperationException("nope");
    }

    public String _relativeOutputDirectory() {
        throw new UnsupportedOperationException("nope");
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder asBuilder() {
        return new Builder();
    }
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        AldorModuleState state = (AldorModuleState) o;
        return true;
        //return (makeConvention == state.makeConvention) && Objects.equals(outputDirectory, state.outputDirectory);
    }

    @SuppressWarnings("ObjectInstantiationInEqualsHashCode")
    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public String toString() {
        return "AldorModuleState{" +
                //"makeConvention=" + makeConvention +
                '}';
    }

    public static class Builder {

        Builder() {
        }

        public AldorModuleState build() {
            return new AldorModuleState(this);
        }

    }

    @Tag("AldorState")
    public static class Wrapper {
        @XmlElement(name = "aldorModuleState")
        @Nullable
        AldorModuleState state;

        public Wrapper() {
            this.state = null;
        }

        public Wrapper(AldorModuleState state) {
            this.state = state;
        }

        public AldorModuleState state() {
            return state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Wrapper wrapper = (Wrapper) o;
            return Objects.equals(state, wrapper.state);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state);
        }
    }
}
