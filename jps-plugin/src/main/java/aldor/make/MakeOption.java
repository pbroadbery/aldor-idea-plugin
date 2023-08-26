package aldor.make;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MakeOption {
    public static MakeOption Parallel = new MakeOption("-j", null);
    public static MakeOption Trace = new MakeOption("--trace", null);
    @NotNull
    private final String optionName;
    @Nullable
    private final String parameter;

    public MakeOption(@NotNull String optionName, @Nullable String parameter) {
        this.optionName = optionName;
        this.parameter = parameter;
    }

    List<String> options() {
        return Optional.ofNullable(parameter).map(p -> List.of(optionName, p)).orElseGet(() -> List.of(optionName));
    }

    @SuppressWarnings({"QuestionableName", "AccessingNonPublicFieldOfAnotherObject"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        MakeOption that = (MakeOption) o;
        return optionName.equals(that.optionName) && Objects.equals(parameter, that.parameter);
    }

    @SuppressWarnings("ObjectInstantiationInEqualsHashCode")
    @Override
    public int hashCode() {
        return Objects.hash(optionName, parameter);
    }
}
