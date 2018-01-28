package aldor.spad;

import aldor.syntax.Syntax;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SpadLibrary {

    List<Syntax> parentCategories(Syntax syntax);
    List<Operation> operations(Syntax syntax);

    @NotNull
    Syntax normalise(@NotNull Syntax syntax);

    class Operation {
        private final String name;
        private final Syntax type;
        private final Syntax condition;
        private final Syntax exporter;

        public Operation(String name, Syntax type, Syntax condition, Syntax exporter) {
            this.name = name;
            this.type = type;
            this.condition = condition;
            this.exporter = exporter;
        }

        @Override
        public String toString() {
            return "{Op: " + name + " " + type + "}";
        }

        public String name() {
            return name;
        }

        public Syntax type() {
            return type;
        }
    }


}
