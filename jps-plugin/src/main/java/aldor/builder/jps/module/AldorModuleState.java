package aldor.builder.jps.module;

import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

// I'd like this to work, but for now all properties have moved to the facet.
public class AldorModuleState {
    @Tag("aldorMakeConvention")
    private MakeConvention makeConvention = null;
    @Nullable
    @Tag("aldorOutputDirectory")
    private String outputDirectory;
    @Tag("aldorRelativeOutputDirectory")
    private String relativeOutputDirectory = null;

    public AldorModuleState() {
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public AldorModuleState(AldorModuleState state) {
        this.makeConvention = state.makeConvention;
        this.outputDirectory = state.outputDirectory;
        this.relativeOutputDirectory = state.relativeOutputDirectory;
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public AldorModuleState(Builder builder) {
        this.makeConvention = builder.makeConvention;
        this.outputDirectory = builder.outputDirectory;
        this.relativeOutputDirectory = builder.relativeOutputDirectory;
    }


    public MakeConvention _makeConvention() {
        return makeConvention;
    }

    public String _outputDirectory() {
        return outputDirectory;
    }

    public String _relativeOutputDirectory() {
        return relativeOutputDirectory;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder asBuilder() {
        return new Builder()
                .outputDirectory(outputDirectory)
                .makeConvention(this.makeConvention)
                .relativeOutputDirectory(relativeOutputDirectory);
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
        return (makeConvention == state.makeConvention) && Objects.equals(outputDirectory, state.outputDirectory);
    }

    @SuppressWarnings("ObjectInstantiationInEqualsHashCode")
    @Override
    public int hashCode() {
        return Objects.hash(makeConvention, outputDirectory);
    }

    @Override
    public String toString() {
        return "AldorModuleState{" +
                "makeConvention=" + makeConvention +
                ", outputDirectory='" + outputDirectory + '\'' +
                '}';
    }

    public static class Builder {
        private MakeConvention makeConvention = MakeConvention.Source;
        private String outputDirectory = "";
        private String relativeOutputDirectory = "";

        Builder() {
        }

        public Builder makeConvention(MakeConvention option) {
            this.makeConvention = option;
            return this;
        }

        public Builder outputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
            return this;
        }

        public AldorModuleState build() {
            return new AldorModuleState(this);
        }

        public Builder relativeOutputDirectory(String relativeOutputDirectory) {
            this.relativeOutputDirectory = relativeOutputDirectory;
            return this;
        }
    }

    @Tag("AldorState")
    public static class Wrapper {
        @XmlElement(name = "aldorModuleState")
        AldorModuleState state = null;

        public Wrapper() {}

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
